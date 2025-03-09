package com.example.estoquevendas;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFSheet;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MercadoActivity extends AppCompatActivity {

    ListView listViewProdutos;
    Button btnFinalizarVenda, btnVoltarMenu, btnLimparCliques;
    CheckBox checkboxCredito, checkboxDebito, checkboxPix, checkboxDinheiro;
    TextView txtTotalVenda;

    double totalVenda = 0.0;
    List<Produto> produtosSelecionados = new ArrayList<>();

    // Variáveis para controlar as instâncias das tarefas
    private CarregarProdutosTask carregarProdutosTask;
    private RegistrarVendaTask registrarVendaTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mercado);

        // Inicializar componentes da interface
        listViewProdutos = findViewById(R.id.listViewProdutos);
        btnFinalizarVenda = findViewById(R.id.btnFinalizarVenda);
        txtTotalVenda = findViewById(R.id.txtTotalVenda);
        btnLimparCliques = findViewById(R.id.btnLimparCliques);
        btnVoltarMenu = findViewById(R.id.btnVoltarMenu);
        checkboxCredito = findViewById(R.id.checkboxCredito);
        checkboxDebito = findViewById(R.id.checkboxDebito);
        checkboxPix = findViewById(R.id.checkboxPix);
        checkboxDinheiro = findViewById(R.id.checkboxDinheiro);

        // Configurar listeners
        btnLimparCliques.setOnClickListener(v -> limparCliques());
        btnFinalizarVenda.setOnClickListener(v -> finalizarVenda());
        btnVoltarMenu.setOnClickListener(v -> finish());

        // Carregar produtos
        carregarProdutos();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Cancelar tarefas em execução ao destruir a Activity
        if (carregarProdutosTask != null && carregarProdutosTask.getStatus() == AsyncTask.Status.RUNNING) {
            carregarProdutosTask.cancel(true);
        }
        if (registrarVendaTask != null && registrarVendaTask.getStatus() == AsyncTask.Status.RUNNING) {
            registrarVendaTask.cancel(true);
        }
    }

    // Método para carregar produtos
    private void carregarProdutos() {
        if (carregarProdutosTask == null || carregarProdutosTask.getStatus() == AsyncTask.Status.FINISHED) {
            carregarProdutosTask = new CarregarProdutosTask();
            carregarProdutosTask.execute();
        }
    }

    // Método para finalizar a venda
    private void finalizarVenda() {
        if (!checkboxCredito.isChecked() && !checkboxDebito.isChecked() && !checkboxPix.isChecked() && !checkboxDinheiro.isChecked()) {
            Toast.makeText(this, "Por favor, selecione uma forma de pagamento.", Toast.LENGTH_SHORT).show();
            return;
        }

        totalVenda = 0.0;
        StringBuilder vendaDetalhes = new StringBuilder("Venda finalizada:\n");

        for (Produto p : produtosSelecionados) {
            if (p.getQuantidade() > 0) {
                double subtotal = p.getValor() * p.getQuantidade();
                totalVenda += subtotal;
                vendaDetalhes.append(p.getNome())
                        .append(" x")
                        .append(p.getQuantidade())
                        .append(" - R$ ")
                        .append(String.format("%.2f", p.getValor()))
                        .append(" = R$ ")
                        .append(String.format("%.2f", subtotal))
                        .append("\n");
            }
        }
        vendaDetalhes.append("Total: R$ ").append(String.format("%.2f", totalVenda)).append("\n");

        Toast.makeText(this, vendaDetalhes.toString(), Toast.LENGTH_LONG).show();

        // Registrar venda
        if (registrarVendaTask == null || registrarVendaTask.getStatus() == AsyncTask.Status.FINISHED) {
            registrarVendaTask = new RegistrarVendaTask();
            registrarVendaTask.execute();
        }
    }

    // Método para atualizar o estoque
    private void atualizarEstoque() {
        try {
            File file = new File(getExternalFilesDir("estoquevendas"), "produtos.xlsx");

            if (!file.exists()) {
                runOnUiThread(() -> Toast.makeText(MercadoActivity.this, "Arquivo de produtos não encontrado!", Toast.LENGTH_LONG).show());
                return;
            }

            FileInputStream fis = new FileInputStream(file);
            XSSFWorkbook workbook = new XSSFWorkbook(fis);
            XSSFSheet sheet = workbook.getSheetAt(0);

            for (Produto produtoVendido : produtosSelecionados) {
                if (produtoVendido.getQuantidade() > 0) {
                    for (Row row : sheet) {
                        if (row.getCell(0) != null && row.getCell(1) != null) {
                            String nomeProduto = row.getCell(0).getStringCellValue();

                            if (nomeProduto.equals(produtoVendido.getNome())) {
                                Cell quantidadeCell = row.getCell(1);

                                int quantidadeAtual;
                                if (quantidadeCell.getCellType() == org.apache.poi.ss.usermodel.CellType.NUMERIC) {
                                    quantidadeAtual = (int) quantidadeCell.getNumericCellValue();
                                } else if (quantidadeCell.getCellType() == org.apache.poi.ss.usermodel.CellType.STRING) {
                                    try {
                                        quantidadeAtual = Integer.parseInt(quantidadeCell.getStringCellValue().trim());
                                    } catch (NumberFormatException e) {
                                        quantidadeAtual = 0;
                                    }
                                } else {
                                    quantidadeAtual = 0;
                                }

                                int novaQuantidade = quantidadeAtual - produtoVendido.getQuantidade();
                                row.getCell(1).setCellValue(Math.max(novaQuantidade, 0));
                                break;
                            }
                        }
                    }
                }
            }

            fis.close();

            FileOutputStream fos = new FileOutputStream(file);
            workbook.write(fos);
            fos.close();
            workbook.close();

            runOnUiThread(() -> Toast.makeText(MercadoActivity.this, "Estoque atualizado!", Toast.LENGTH_SHORT).show());
        } catch (IOException e) {
            e.printStackTrace();
            runOnUiThread(() -> Toast.makeText(MercadoActivity.this, "Erro ao atualizar o estoque!", Toast.LENGTH_SHORT).show());
        }
    }

    // Método para limpar os cliques de produtos
    private void limparCliques() {
        for (Produto p : produtosSelecionados) {
            p.setQuantidade(0);
        }
        atualizarTotalVenda();
    }

    // Método para atualizar o total da venda
    private void atualizarTotalVenda() {
        totalVenda = 0.0;
        for (Produto p : produtosSelecionados) {
            totalVenda += p.getQuantidade() * p.getValor();
        }
        txtTotalVenda.setText("Total: R$ " + String.format("%.2f", totalVenda));
    }

    // Método para obter as formas de pagamento selecionadas
    private String obterFormasDePagamentoSelecionadas() {
        StringBuilder formasPagamento = new StringBuilder();
        if (checkboxCredito.isChecked()) {
            formasPagamento.append("Crédito ");
        }
        if (checkboxDebito.isChecked()) {
            formasPagamento.append("Débito ");
        }
        if (checkboxPix.isChecked()) {
            formasPagamento.append("Pix ");
        }
        if (checkboxDinheiro.isChecked()) {
            formasPagamento.append("Dinheiro ");
        }
        return formasPagamento.toString().trim();
    }

    // AsyncTask para carregar produtos
    private class CarregarProdutosTask extends AsyncTask<Void, Void, List<String>> {
        @Override
        protected List<String> doInBackground(Void... voids) {
            List<String> produtos = new ArrayList<>();
            try {
                File file = new File(getExternalFilesDir("estoquevendas"), "produtos.xlsx");

                if (!file.exists()) {
                    runOnUiThread(() -> Toast.makeText(MercadoActivity.this, "Arquivo de produtos não encontrado!", Toast.LENGTH_LONG).show());
                    return produtos;
                }

                FileInputStream fis = new FileInputStream(file);
                XSSFWorkbook workbook = new XSSFWorkbook(fis);
                XSSFSheet sheet = workbook.getSheetAt(0);

                for (Row row : sheet) {
                    if (row.getCell(0) != null && row.getCell(2) != null) {
                        String nomeProduto = row.getCell(0).getStringCellValue();
                        double valorProduto = obterValorProduto(row.getCell(2));

                        if (valorProduto >= 0) {
                            Produto produto = new Produto(nomeProduto, 0, valorProduto);
                            produtos.add(nomeProduto + " - R$ " + valorProduto);
                            produtosSelecionados.add(produto);
                        }
                    }
                }

                fis.close();
            } catch (IOException e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(MercadoActivity.this, "Erro ao carregar produtos da planilha!", Toast.LENGTH_SHORT).show());
            }
            return produtos;
        }

        @Override
        protected void onPostExecute(List<String> produtos) {
            if (produtos.isEmpty()) {
                Toast.makeText(MercadoActivity.this, "Nenhum produto encontrado!", Toast.LENGTH_SHORT).show();
            } else {
                ArrayAdapter<String> adapter = new ArrayAdapter<>(MercadoActivity.this, android.R.layout.simple_list_item_1, produtos);
                listViewProdutos.setAdapter(adapter);

                listViewProdutos.setOnItemClickListener((parent, view, position, id) -> {
                    Produto produto = produtosSelecionados.get(position);
                    produto.setQuantidade(produto.getQuantidade() + 1);
                    atualizarTotalVenda();
                });
            }
        }
    }

    // AsyncTask para registrar a venda
    private class RegistrarVendaTask extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                File dir = new File(getExternalFilesDir(null), "estoquevendas");

                if (!dir.exists()) {
                    dir.mkdirs();
                }

                File file = new File(dir, "registros.xlsx");
                boolean isNovoArquivo = !file.exists();

                XSSFWorkbook workbook;
                XSSFSheet sheet;

                if (isNovoArquivo) {
                    workbook = new XSSFWorkbook();
                    sheet = workbook.createSheet("Registros");

                    Row headerRow = sheet.createRow(0);
                    headerRow.createCell(0).setCellValue("Produto");
                    headerRow.createCell(1).setCellValue("Quantidade");
                    headerRow.createCell(2).setCellValue("Valor Unitário");
                    headerRow.createCell(3).setCellValue("Valor Total");
                    headerRow.createCell(4).setCellValue("Forma de Pagamento");
                    headerRow.createCell(5).setCellValue("Data/Hora");
                } else {
                    FileInputStream fis = new FileInputStream(file);
                    workbook = new XSSFWorkbook(fis);
                    sheet = workbook.getSheetAt(0);
                }

                String formasDePagamento = obterFormasDePagamentoSelecionadas();
                String dataHora = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date());

                for (Produto produto : produtosSelecionados) {
                    if (produto.getQuantidade() > 0) {
                        Row row = sheet.createRow(sheet.getPhysicalNumberOfRows());
                        row.createCell(0).setCellValue(produto.getNome());
                        row.createCell(1).setCellValue(produto.getQuantidade());
                        row.createCell(2).setCellValue(produto.getValor());
                        row.createCell(3).setCellValue(produto.getQuantidade() * produto.getValor());
                        row.createCell(4).setCellValue(formasDePagamento);
                        row.createCell(5).setCellValue(dataHora);
                    }
                }

                FileOutputStream fos = new FileOutputStream(file);
                workbook.write(fos);
                fos.close();
                workbook.close();

                atualizarEstoque();
                return true;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean sucesso) {
            if (sucesso) {
                Toast.makeText(MercadoActivity.this, "Venda registrada com sucesso!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(MercadoActivity.this, "Erro ao registrar a venda!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Método para obter o valor do produto
    private double obterValorProduto(Cell cell) {
        if (cell.getCellType() == org.apache.poi.ss.usermodel.CellType.NUMERIC) {
            return cell.getNumericCellValue();
        } else if (cell.getCellType() == org.apache.poi.ss.usermodel.CellType.STRING) {
            try {
                return Double.parseDouble(cell.getStringCellValue().trim());
            } catch (NumberFormatException e) {
                return -1;
            }
        }
        return -1;
    }
}