import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.Semaphore;

public class App extends JFrame {


    private ArrayList<String> cartas;
    private ArrayList<JButton> botoes;
    private JButton primeiroBotao, segundoBotao;
    private int tentativas;
    private JLabel labelTempo, labelTentativas;
    private Timer temporizador;
    private Semaphore semaforo;

    public App() {
        cartas = new ArrayList<>();
        cartas.add("A");
        cartas.add("B");
        cartas.add("C");
        cartas.add("D");
        cartas.add("E");
        cartas.add("F");
        cartas.add("G");
        cartas.add("H");

        // Duplica as cartas para ter pares
        cartas.addAll(cartas);

        // Embaralha as cartas
        Collections.shuffle(cartas);

        botoes = new ArrayList<>();

        Container container = getContentPane();
        container.setLayout(new BorderLayout());

        JPanel painelBotoes = new JPanel();
        painelBotoes.setLayout(new GridLayout(4, 4, 5, 5));

        for (int i = 0; i < cartas.size(); i++) {
            JButton botao = new JButton();
            botao.setPreferredSize(new Dimension(80, 80));
            botao.setIcon(new ImageIcon(getClass().getResource("/assets/card_back.png")));
            botao.addActionListener(new BotaoListener());
            botoes.add(botao);
            painelBotoes.add(botao);
        }

        container.add(painelBotoes, BorderLayout.CENTER);

        JPanel painelInformacoes = new JPanel();
        labelTempo = new JLabel("Tempo: 0");
        labelTentativas = new JLabel("Tentativas: 0");
        painelInformacoes.add(labelTempo);
        painelInformacoes.add(labelTentativas);
        JButton reiniciarBotao = new JButton("Reiniciar Jogo");
        reiniciarBotao.addActionListener(new ReiniciarListener());
        painelInformacoes.add(reiniciarBotao);

        container.add(painelInformacoes, BorderLayout.SOUTH);

        semaforo = new Semaphore(1);
        temporizador = new Timer(1000, new TemporizadorListener());
        temporizador.start();

        tentativas = 0;

        setSize(600, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private class BotaoListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (!temporizador.isRunning()) {
                temporizador.start();
            }

            JButton botao = (JButton) e.getSource();

            if (primeiroBotao == null) {
                primeiroBotao = botao;
                primeiroBotao.setIcon(new ImageIcon(
                        getClass().getResource("/assets/card_" + cartas.get(botoes.indexOf(botao)) + ".png")));
                primeiroBotao.setEnabled(true);
            } else if (segundoBotao == null && !botao.equals(primeiroBotao)) {
                segundoBotao = botao;
                segundoBotao.setIcon(new ImageIcon(
                        getClass().getResource("/assets/card_" + cartas.get(botoes.indexOf(botao)) + ".png")));
                segundoBotao.setEnabled(true);

                tentativas++;

                labelTentativas.setText("Tentativas: " + tentativas);

                // Verifica se as cartas coincidem
                if (cartas.get(botoes.indexOf(primeiroBotao)).equals(cartas.get(botoes.indexOf(segundoBotao)))) {
                    JOptionPane.showMessageDialog(App.this, "Par encontrado!");
                    primeiroBotao = null;
                    segundoBotao = null;

                    // Verifica se o jogador venceu
                    if (verificarVitoria()) {
                        temporizador.stop();
                        JOptionPane.showMessageDialog(App.this, "Parabéns! Você venceu!");
                    }
                } else {
                    // Se não coincidirem, vira as cartas em uma thread separada
                    new Thread(() -> {
                        try {
                            Thread.sleep(1000); // Aguarda 1 segundo antes de virar as cartas
                        } catch (InterruptedException ex) {
                            ex.printStackTrace();
                        }

                        SwingUtilities.invokeLater(() -> {
                            virarCartas();
                        });
                    }).start();
                }
            }
        }
    }

    private class TemporizadorListener implements ActionListener {
        private int tempo;

        public TemporizadorListener() {
            tempo = 0;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            tempo++;
            labelTempo.setText("Tempo: " + tempo + "s");

            // Adicione um limite de tempo aqui (por exemplo, 60 segundos)
            if (tempo == 60) {
                temporizador.stop();
                JOptionPane.showMessageDialog(App.this, "Tempo esgotado! Você perdeu.");
            }
        }
    }

    private class ReiniciarListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            reiniciarJogo();
        }
    }

    private void reiniciarJogo() {
        temporizador.stop();
        temporizador = new Timer(1000, new TemporizadorListener());
        temporizador.start();

        tentativas = 0;
        labelTentativas.setText("Tentativas: 0");

        for (JButton botao : botoes) {
            botao.setIcon(new ImageIcon(getClass().getResource("/assets/card_back.png")));
            botao.setEnabled(true);
        }

        // Embaralha as cartas
        Collections.shuffle(cartas);

        primeiroBotao = null;
        segundoBotao = null;
    }

    private void virarCartas() {
        try {
            semaforo.acquire();
            primeiroBotao.setIcon(new ImageIcon(getClass().getResource("/assets/card_back.png")));
            segundoBotao.setIcon(new ImageIcon(getClass().getResource("/assets/card_back.png")));
            primeiroBotao.setEnabled(true);
            segundoBotao.setEnabled(true);
            primeiroBotao = null;
            segundoBotao = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            semaforo.release();
        }
    }

    private boolean verificarVitoria() {
        for (JButton botao : botoes) {
            if (botao.isEnabled()) {
                return false;
            }
        }
        return true;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new App());
    }
}
