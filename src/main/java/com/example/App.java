package com.example;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;


/**
 * Classe principal da aplicação JavaFX para gerenciamento de eventos, palestras
 * e participantes.
 * 
 * @author Grupo 1:
 * Ana Gomes Souza,
 * Arthur Sousa Costa,
 * Eduardo Miranda Berlink Santos,
 * Henrique Rezende Bandeira Chiachio,
 * João Lucas Fonseca Chagas,
 * Marco Antonio Barbosa Pereira,
 * Mary Nicole de Sousa Mendes,
 * Pedro César Padre Lima
 * 
 * 
 * @since 28-05-2025
 * @version 1.0
 */
public class App extends Application {

    /** Lista personalizada para armazenar os eventos */
    private Lista eventos = new Lista(20);

    private ListView<Evento> lvEventos;
    private ListView<Palestra> lvPalestras;
    private TextArea taDetalhes;

    /** Formato de data utilizado nas interfaces */
    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    /**
     * Inicializa a interface gráfica da aplicação.
     *
     * @param primaryStage Janela principal do JavaFX.
     */
    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Sistema de Gerenciamento de Eventos");

        // Lista de eventos
        lvEventos = new ListView<>();
        lvEventos.setPrefWidth(250);
        lvEventos.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            mostrarPalestrasEvento(newVal);
        });

        // Lista de palestras do evento selecionado
        lvPalestras = new ListView<>();
        lvPalestras.setPrefWidth(300);
        lvPalestras.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            mostrarDetalhesPalestra(newVal);
        });

        // Área de exibição de detalhes
        taDetalhes = new TextArea();
        taDetalhes.setEditable(false);
        taDetalhes.setPrefWidth(300);
        taDetalhes.setPrefHeight(150);

        // Botões de ação
        Button btnAddEvento = new Button("Adicionar Evento");
        btnAddEvento.setOnAction(e -> adicionarEvento());

        Button btnAddPalestra = new Button("Adicionar Palestra");
        btnAddPalestra.setOnAction(e -> adicionarPalestra());

        Button btnInscrever = new Button("Inscrever Participante");
        btnInscrever.setOnAction(e -> inscreverParticipante());

        HBox hBoxBotoes = new HBox(10, btnAddEvento, btnAddPalestra, btnInscrever);
        hBoxBotoes.setPadding(new Insets(10));


        HBox hBoxListas = new HBox(10, lvEventos, lvPalestras, taDetalhes);
        hBoxListas.setPadding(new Insets(10));

        VBox vBoxPrincipal = new VBox(10, hBoxListas, hBoxBotoes);
        vBoxPrincipal.setPadding(new Insets(10));

        Scene scene = new Scene(vBoxPrincipal, 900, 400);
        primaryStage.setScene(scene);
        primaryStage.show();

        carregarDadosTeste(); // Dados iniciais para testes
    }

    /**
     * Exibe palestras relacionadas ao evento selecionado.
     *
     * @param evento Evento selecionado.
     */
    private void mostrarPalestrasEvento(Evento evento) {
        lvPalestras.getItems().clear();
        taDetalhes.clear();
        if (evento != null) {
            taDetalhes.setText(evento.toString());
            Palestra[] palestras = evento.listarPalestras();
            lvPalestras.getItems().addAll(palestras);
        }
    }

    /**
     * Mostra os detalhes de uma palestra, incluindo participantes inscritos.
     *
     * @param palestra Palestra selecionada.
     */
    private void mostrarDetalhesPalestra(Palestra palestra) {
        taDetalhes.clear();
        if (palestra != null) {
            StringBuilder sb = new StringBuilder();
            sb.append(palestra.toString()).append("\n\n");
            sb.append("Participantes Inscritos:\n");

            Participante[] participantes = palestra.listarParticipantes();
            if (participantes != null && participantes.length > 0) {
                for (Participante p : participantes) {
                    if (p != null) {
                        sb.append("- ").append(p.getNome()).append(" (").append(p.getEmail()).append(")\n");
                    }
                }
            } else {
                sb.append("Nenhum participante inscrito.\n");
            }

            taDetalhes.setText(sb.toString());
        }
    }

    /**
     * Abre uma caixa de diálogo para adicionar um novo evento.
     */
    private void adicionarEvento() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Adicionar Evento");
        dialog.setHeaderText("Informe os dados do evento (nome, descrição, data início, data fim):");
        dialog.setContentText("Formato: nome;descrição;dd/MM/yyyy;dd/MM/yyyy");

        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            try {
                String[] partes = result.get().split(";");
                if (partes.length == 4) {
                    String nome = partes[0].trim();
                    String descricao = partes[1].trim();
                    LocalDate dataInicio = LocalDate.parse(partes[2].trim(), formatter);
                    LocalDate dataFim = LocalDate.parse(partes[3].trim(), formatter);

                    Evento evento = new Evento("EV" + (eventos.getTamanho() + 1), nome, descricao, dataInicio, dataFim);
                    eventos.anexar(evento);
                    atualizarListaEventos();
                } else {
                    mostrarAlerta("Erro", "Dados incompletos. Use o formato correto.");
                }
            } catch (Exception ex) {
                mostrarAlerta("Erro", "Formato de data inválido.");
            }
        }
    }

    /**
     * Adiciona uma nova palestra ao evento selecionado.
     */
    private void adicionarPalestra() {
        Evento eventoSelecionado = lvEventos.getSelectionModel().getSelectedItem();
        if (eventoSelecionado == null) {
            mostrarAlerta("Aviso", "Selecione um evento antes de adicionar palestra.");
            return;
        }

        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Adicionar Palestra");
        dialog.setHeaderText(
                "Informe os dados da palestra (id;título;palestrante;local;hora início;hora fim;descrição):");
        dialog.setContentText("Exemplo: P1;Título;Palestrante;Sala 1;09:00;10:00;Descrição");

        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            try {
                String[] partes = result.get().split(";");
                if (partes.length == 7) {
                    String id = partes[0].trim();
                    String titulo = partes[1].trim();
                    String palestrante = partes[2].trim();
                    String local = partes[3].trim();
                    String horarioInicio = partes[4].trim();
                    String horarioFinal = partes[5].trim();
                    String descricao = partes[6].trim();

                    Palestra palestra = new Palestra(id, titulo, descricao, null, null, 0, local, palestrante, 0);

                    boolean adicionou = eventoSelecionado.adicionarPalestra(palestra);
                    if (adicionou) {
                        mostrarPalestrasEvento(eventoSelecionado);
                    } else {
                        mostrarAlerta("Erro", "Não foi possível adicionar a palestra (conflito ou erro).");
                    }
                } else {
                    mostrarAlerta("Erro", "Dados incompletos. Use o formato correto.");
                }
            } catch (Exception ex) {
                mostrarAlerta("Erro", "Erro ao adicionar palestra: " + ex.getMessage());
            }
        }
    }

    /**
     * Inscreve um participante na palestra selecionada.
     */
    private void inscreverParticipante() {
        Palestra palestraSelecionada = lvPalestras.getSelectionModel().getSelectedItem();
        if (palestraSelecionada == null) {
            mostrarAlerta("Aviso", "Selecione uma palestra para inscrever participante.");
            return;
        }

        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Inscrever Participante");
        dialog.setHeaderText("Informe os dados do participante (id;nome;email):");
        dialog.setContentText("Exemplo: U1;João Silva;joao@email.com");

        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            try {
                String[] partes = result.get().split(";");
                if (partes.length == 3) {
                    String id = partes[0].trim();
                    String nome = partes[1].trim();
                    String email = partes[2].trim();

                    Participante participante = new Participante(id, nome, email);
                    boolean inscrito = palestraSelecionada.inscreverParticipante(participante);
                    if (inscrito) {
                        participante.inscreverEmPalestra(palestraSelecionada);
                        mostrarAlerta("Sucesso", "Participante inscrito com sucesso.");
                    } else {
                        mostrarAlerta("Erro",
                                "Não foi possível inscrever participante (palestra cheia ou já inscrito).");
                    }
                } else {
                    mostrarAlerta("Erro", "Dados incompletos. Use o formato correto.");
                }
            } catch (Exception ex) {
                mostrarAlerta("Erro", "Erro ao inscrever participante: " + ex.getMessage());
            }
        }
    }

    /**
     * Atualiza a lista visual de eventos exibida na interface.
     */
    private void atualizarListaEventos() {
        lvEventos.getItems().clear();
        Object[] eventosArray = eventos.selecionarTodos();
        for (Object obj : eventosArray) {
            lvEventos.getItems().add((Evento) obj);
        }
    }

    /**
     * Exibe uma janela de alerta com uma mensagem personalizada.
     *
     * @param titulo   Título da janela.
     * @param mensagem Conteúdo da mensagem.
     */
    private void mostrarAlerta(String titulo, String mensagem) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }

    /**
     * Carrega dados de exemplo para facilitar o teste da aplicação.
     */
    private void carregarDadosTeste() {
        try {
            Evento e1 = new Evento("EV1", "Java Conference", "Conferência sobre Java", LocalDate.now(),
                    LocalDate.now().plusDays(2));
            Palestra p1 = new Palestra(STYLESHEET_CASPIAN, STYLESHEET_CASPIAN, STYLESHEET_CASPIAN, null, null, 0,
                    STYLESHEET_MODENA, STYLESHEET_CASPIAN, 0);
            Palestra p2 = new Palestra(STYLESHEET_CASPIAN, STYLESHEET_CASPIAN, STYLESHEET_CASPIAN, null, null, 0,
                    STYLESHEET_MODENA, STYLESHEET_CASPIAN, 0);
            e1.adicionarPalestra(p1);
            e1.adicionarPalestra(p2);
            
            eventos.anexar(e1);
            atualizarListaEventos();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Método principal. Lança a aplicação JavaFX.
     *
     * @param args Argumentos da linha de comando.
     */
    public static void main(String[] args) {
        launch(args);
    }
}
