package com.example.estoquevendas;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

public class BestoqueActivity extends AppCompatActivity {

    ListView listViewProdutos;

    Button btnRetornarMain;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista_bestoque);

        Log.d("BestoqueActivity", "onCreate() chamado!");

        listViewProdutos = findViewById(R.id.listViewProdutos);

        btnRetornarMain = findViewById(R.id.btnRetornarMain);



        // Configurando o botão para voltar para a MainActivity
        btnRetornarMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(BestoqueActivity.this, MainActivity.class);
                startActivity(intent); // Inicia a MainActivity
                finish(); // Finaliza a BestoqueActivity
            }
        });

        // Carrega todos os produtos ao abrir a tela
        try {
            carregarBaixoEstoque();
        } catch (Exception e) {
            Toast.makeText(BestoqueActivity.this, "Erro ao carregar baixo estoque!", Toast.LENGTH_SHORT).show();
            Log.e("BestoqueActivity", "Erro ao carregar baixo estoque", e);
        }
    }

    private void carregarBaixoEstoque() {
        try {
            File pasta = new File(getExternalFilesDir(null), "estoquevendas");
            if (!pasta.exists()) {
                pasta.mkdirs();
            }

            File file = new File(pasta, "produtos.xlsx");

            if (!file.exists()) {
                Toast.makeText(BestoqueActivity.this, "Arquivo não encontrado!", Toast.LENGTH_LONG).show();
                Log.d("BestoqueActivity", "Arquivo não encontrado no caminho: " + file.getAbsolutePath());
                return;
            }

            FileInputStream fis = new FileInputStream(file);
            XSSFWorkbook workbook = new XSSFWorkbook(fis);
            XSSFSheet sheet = workbook.getSheetAt(0);

            List<String> produtosBaixoEstoque = new ArrayList<>();

            for (Row row : sheet) {
                if (row.getCell(0) != null && row.getCell(0).getCellType() == org.apache.poi.ss.usermodel.CellType.STRING) {
                    String nomeProduto = row.getCell(0).getStringCellValue();

                    double quantidade = 0;
                    if (row.getCell(1) != null) {
                        if (row.getCell(1).getCellType() == org.apache.poi.ss.usermodel.CellType.NUMERIC) {
                            quantidade = row.getCell(1).getNumericCellValue();
                        } else if (row.getCell(1).getCellType() == org.apache.poi.ss.usermodel.CellType.STRING) {
                            try {
                                quantidade = Double.parseDouble(row.getCell(1).getStringCellValue());
                            } catch (NumberFormatException e) {
                                quantidade = 0;
                            }
                        }
                    }

                    double valor = 0;
                    if (row.getCell(2) != null) {
                        if (row.getCell(2).getCellType() == org.apache.poi.ss.usermodel.CellType.NUMERIC) {
                            valor = row.getCell(2).getNumericCellValue();
                        } else if (row.getCell(2).getCellType() == org.apache.poi.ss.usermodel.CellType.STRING) {
                            try {
                                valor = Double.parseDouble(row.getCell(2).getStringCellValue());
                            } catch (NumberFormatException e) {
                                valor = 0;
                            }
                        }
                    }

                    // Verifica se a quantidade é menor que 10 e maior que 0 antes de adicionar o produto à lista
                    if (quantidade < 10 && quantidade > 0 && nomeProduto != null && !nomeProduto.isEmpty()) {
                        String produto = "Nome: " + nomeProduto + " | Quantidade: " + quantidade + " | Valor: R$ " + valor;
                        produtosBaixoEstoque.add(produto);
                    }
                }
            }

            // Se houver produtos de baixo estoque, os exibe, caso contrário exibe uma mensagem.
            if (produtosBaixoEstoque.isEmpty()) {
                produtosBaixoEstoque.add("Nenhum produto com baixo estoque.");
            }

            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, produtosBaixoEstoque);
            listViewProdutos.setAdapter(adapter);

            fis.close();

        } catch (Exception e) {
            Toast.makeText(BestoqueActivity.this, "Erro ao carregar baixo estoque!", Toast.LENGTH_SHORT).show();
            Log.e("BestoqueActivity", "Erro ao carregar baixo estoque", e);
        }
    }
    }