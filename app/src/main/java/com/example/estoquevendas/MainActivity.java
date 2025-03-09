package com.example.estoquevendas;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;

public class MainActivity extends AppCompatActivity {

    // Referências aos botões da Sidebar
    Button btnCadastrarProdutoSidebar, btnListaProdutoSidebar, btnRegistro_lista, btnMercadoSidebar, btnDescarteProdutoSidebar,btnEstoqueBaixo, btnExcluirProduto;
    DrawerLayout drawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Referências aos botões da Sidebar
        btnCadastrarProdutoSidebar = findViewById(R.id.btnCadastrarProdutoSidebar);
        btnListaProdutoSidebar = findViewById(R.id.btnListaProdutoSidebar);
        btnMercadoSidebar = findViewById(R.id.btnMercadoSidebar);
        btnExcluirProduto = findViewById(R.id.btnExcluirProduto);
        btnDescarteProdutoSidebar = findViewById(R.id.btnDescarteProdutoSidebar);
        btnEstoqueBaixo =findViewById(R.id.btnEstoqueBaixo);
        btnRegistro_lista=findViewById(R.id.btnRegistro_lista);
        // Referência ao DrawerLayout
        drawerLayout = findViewById(R.id.drawer_layout);

        // Configuração do DrawerToggle (ícone do menu)
        Toolbar toolbar = findViewById(R.id.toolbar); // Se você tiver uma Toolbar configurada
        setSupportActionBar(toolbar);
        toolbar.setBackgroundColor(getResources().getColor(R.color.laranja));


        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar,
                R.string.drawer_open, R.string.drawer_close);

        drawerLayout.addDrawerListener(toggle);
        getSupportActionBar().setTitle("Master Estoque");

        toggle.syncState();

        // Ações dos botões da Sidebar
        btnCadastrarProdutoSidebar.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SenhaActivity.class);
            intent.putExtra("proximaActivity", "CadastrarProduto");
            startActivity(intent);
        });

        btnDescarteProdutoSidebar.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SenhaActivity.class);
            intent.putExtra("proximaActivity", "DescarteProduto");
            startActivity(intent);
        });

        btnExcluirProduto.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SenhaActivity.class);
            intent.putExtra("proximaActivity", "ExcluirActivity");
            startActivity(intent);
        });

        btnRegistro_lista.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SenhaActivity.class);
            intent.putExtra("proximaActivity", "RegistrosActivity");
            startActivity(intent);

        });
        btnListaProdutoSidebar.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, ListaProdutosActivity.class));
        });

        btnMercadoSidebar.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, MercadoActivity.class));
        });

        btnEstoqueBaixo.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, BestoqueActivity.class));
        });

    }
}
