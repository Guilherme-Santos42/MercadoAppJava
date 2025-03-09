package com.example.estoquevendas;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class SenhaActivity extends AppCompatActivity {

    EditText edtSenha;
    Button btnAcessar, btnVoltarMenu;


    // Senha correta
    private static final String SENHA_CORRETA = "3131";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_senha);

        edtSenha = findViewById(R.id.edtSenha);
        btnAcessar = findViewById(R.id.btnAcessar);
        btnVoltarMenu = findViewById(R.id.btnVoltarMenu);

        btnAcessar.setOnClickListener(v -> {
            String senhaDigitada = edtSenha.getText().toString();

            if (senhaDigitada.equals(SENHA_CORRETA)) {
                // Verifica qual activity deve ser aberta após a senha correta
                String proximaActivity = getIntent().getStringExtra("proximaActivity");

                if ("CadastrarProduto".equals(proximaActivity)) {
                    Intent intent = new Intent(SenhaActivity.this, CadastrarProdutoActivity.class);
                    startActivity(intent);
                } else if ("DescarteProduto".equals(proximaActivity)) {
                    Intent intent = new Intent(SenhaActivity.this, DescarteProdutoActivity.class);
                    startActivity(intent);
                } else if ("ExcluirActivity".equals(proximaActivity)) {
                    Intent intent = new Intent(SenhaActivity.this, ExcluirActivity.class);
                    startActivity(intent);
                } else if ("RegistrosActivity".equals(proximaActivity)) {
                    Intent intent = new Intent(SenhaActivity.this, RegistrosActivity.class);
                    startActivity(intent);
                }
                finish(); // Finaliza a activity de senha para não voltar nela
            } else {
                // Senha incorreta, mostra um aviso
                Toast.makeText(SenhaActivity.this, "Senha incorreta!", Toast.LENGTH_SHORT).show();
            }
        });

        btnVoltarMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Cria uma Intent para voltar à atividade principal
                Intent intent = new Intent(SenhaActivity.this, MainActivity.class);
                startActivity(intent);
                finish(); // Finaliza a atividade atual para que o usuário não volte a ela ao pressionar o botão de "Voltar"
            }
        });
    }
}

