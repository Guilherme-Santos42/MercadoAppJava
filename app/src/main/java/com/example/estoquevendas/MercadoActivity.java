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

        // Chamar o AsyncTask para carregar produtos após inicializar componentes
        new CarregarProdutosTask().execute();
    }

    // Método para carregar produtos da planilha em segundo plano
    private class CarregarProdutosTask extends AsyncTask<Void, Void, List<String>> {
        @Override
        protected List<String> doInBackground(Void... voids) {
            List<String> produtos = new ArrayList<>();
            try {
                // Acessando o diretório privado do aplicativo em android/data/com.example.estoquevendas/files/
                File file = new File(getExternalFilesDir("estoquevendas"), "produtos.xlsx");

                // Verificando se o arquivo existe no diretório privado
                if (!file.exists()) {
                    runOnUiThread(() -> Toast.makeText(MercadoActivity.this, "Arquivo de produtos não encontrado!", Toast.LENGTH_LONG).show());
                    return produtos;
                }

                // Abrindo a planilha XLSX
                FileInputStream fis = new FileInputStream(file);
                XSSFWorkbook workbook = new XSSFWorkbook(fis);
                XSSFSheet sheet = workbook.getSheetAt(0);

                // Processando os dados da planilha
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

    // Método para obter o valor do produto
    private double obterValorProduto(Cell cell) {
        if (cell.getCellType() == org.apache.poi.ss.usermodel.CellType.NUMERIC) {
            return cell.getNumericCellValue();
        } else if (cell.getCellType() == org.apache.poi.ss.usermodel.CellType.STRING) {
            try {
                return Double.parseDouble(cell.getStringCellValue().trim());
            } catch (NumberFormatException e) {
                return -1; // Valor inválido
            }
        }
        return -1; // Valor inválido
    }

    // Método para finalizar a venda
    private void finalizarVenda() {
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
                        .append(p.getValor())
                        .append(" = R$ ")
                        .append(subtotal)
                        .append("\n");
            }
        }
        vendaDetalhes.append("Total: R$ ").append(totalVenda).append("\n");

        Toast.makeText(this, vendaDetalhes.toString(), Toast.LENGTH_LONG).show();
        new RegistrarVendaTask().execute(); // Registrar venda em segundo plano
    }

    // Método para registrar a venda na planilha em segundo plano
    private class RegistrarVendaTask extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                // Acessando o diretório correto em android/data/com.example.estoquevendas/files/estoquevendas
                File dir = new File(getExternalFilesDir(null), "estoquevendas");

                if (!dir.exists()) {
                    dir.mkdirs(); // Criando diretório caso não exista
                }

                // Criando ou abrindo o arquivo de registros
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

                // Adicionando os dados da venda
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
        txtTotalVenda.setText("Total: R$ " + totalVenda);
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
}
