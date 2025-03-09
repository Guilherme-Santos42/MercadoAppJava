package com.example.estoquevendas;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class RegistrosActivity extends AppCompatActivity {

    ListView listViewRegistros;
    TextView tvTotalBruto, tvDataSelecionada;
    Button btnRetornarMain, btnEscolherData;

    Calendar dataSelecionada;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista_registros);

        Log.d("RegistrosActivity", "onCreate() chamado!");

        listViewRegistros = findViewById(R.id.listViewRegistros);
        tvTotalBruto = findViewById(R.id.tvTotalBruto);
        tvDataSelecionada = findViewById(R.id.tvDataSelecionada);
        btnRetornarMain = findViewById(R.id.btnRetornarMain);
        btnEscolherData = findViewById(R.id.btnEscolherData);

        // Inicializa a data com o valor atual
        dataSelecionada = Calendar.getInstance();

        // Configura o botão para escolher a data específica
        btnEscolherData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog();
            }
        });

        // Configura o botão para voltar para a MainActivity
        btnRetornarMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RegistrosActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

        // Carrega os registros ao abrir a tela
        try {
            carregarRegistrosFiltrados();
        } catch (Exception e) {
            Toast.makeText(RegistrosActivity.this, "Erro ao carregar registros!", Toast.LENGTH_SHORT).show();
            Log.e("RegistrosActivity", "Erro ao carregar registros", e);
        }
    }

    private void showDatePickerDialog() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                RegistrosActivity.this,
                (view, year, monthOfYear, dayOfMonth) -> {
                    dataSelecionada.set(Calendar.YEAR, year);
                    dataSelecionada.set(Calendar.MONTH, monthOfYear);
                    dataSelecionada.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                    tvDataSelecionada.setText("Data: " + sdf.format(dataSelecionada.getTime()));

                    // Após a seleção da data, filtramos os registros
                    try {
                        carregarRegistrosFiltrados();
                    } catch (Exception e) {
                        Toast.makeText(RegistrosActivity.this, "Erro ao carregar registros!", Toast.LENGTH_SHORT).show();
                        Log.e("RegistrosActivity", "Erro ao carregar registros", e);
                    }
                },
                dataSelecionada.get(Calendar.YEAR),
                dataSelecionada.get(Calendar.MONTH),
                dataSelecionada.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private void carregarRegistrosFiltrados() {
        try {
            File pasta = new File(getExternalFilesDir(null), "estoquevendas");
            if (!pasta.exists()) {
                pasta.mkdirs();
            }

            // A planilha agora é 'registros.xlsx'
            File file = new File(pasta, "registros.xlsx");

            if (!file.exists()) {
                Toast.makeText(RegistrosActivity.this, "Arquivo não encontrado!", Toast.LENGTH_LONG).show();
                Log.d("RegistrosActivity", "Arquivo não encontrado no caminho: " + file.getAbsolutePath());
                return;
            }

            FileInputStream fis = new FileInputStream(file);
            XSSFWorkbook workbook = new XSSFWorkbook(fis);
            XSSFSheet sheet = workbook.getSheetAt(0);

            List<String> registrosFiltrados = new ArrayList<>();
            double totalBruto = 0;

            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

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

                    double valorUnitario = 0;
                    if (row.getCell(2) != null) {
                        if (row.getCell(2).getCellType() == org.apache.poi.ss.usermodel.CellType.NUMERIC) {
                            valorUnitario = row.getCell(2).getNumericCellValue();
                        } else if (row.getCell(2).getCellType() == org.apache.poi.ss.usermodel.CellType.STRING) {
                            try {
                                valorUnitario = Double.parseDouble(row.getCell(2).getStringCellValue());
                            } catch (NumberFormatException e) {
                                valorUnitario = 0;
                            }
                        }
                    }

                    // Filtra pela data
                    String dataRegistro = row.getCell(5).getStringCellValue();

                    try {
                        Calendar dataProduto = Calendar.getInstance();
                        dataProduto.setTime(sdf.parse(dataRegistro));

                        // Filtra pela data (comparando se a data é igual à selecionada)
                        if (dataProduto.get(Calendar.YEAR) == dataSelecionada.get(Calendar.YEAR) &&
                                dataProduto.get(Calendar.MONTH) == dataSelecionada.get(Calendar.MONTH) &&
                                dataProduto.get(Calendar.DAY_OF_MONTH) == dataSelecionada.get(Calendar.DAY_OF_MONTH)) {
                            double valorBruto = quantidade * valorUnitario;
                            registrosFiltrados.add("Nome: " + nomeProduto + " | Quantidade: " + quantidade + " | Valor Unitário: R$ " + valorUnitario + " | Valor Bruto: R$ " + valorBruto);
                            totalBruto += valorBruto;
                        }
                    } catch (Exception e) {
                        // Ignorar erros de conversão de data
                    }
                }
            }

            if (registrosFiltrados.isEmpty()) {
                registrosFiltrados.add("Nenhum registro encontrado.");
            }

            tvTotalBruto.setText("Valor Total Bruto: R$ " + totalBruto);
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, registrosFiltrados);
            listViewRegistros.setAdapter(adapter);

            fis.close();

        } catch (Exception e) {
            Toast.makeText(RegistrosActivity.this, "Erro ao carregar registros!", Toast.LENGTH_SHORT).show();
            Log.e("RegistrosActivity", "Erro ao carregar registros", e);
        }
    }
}