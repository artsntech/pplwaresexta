package gui;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import buffer.Buffer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.paint.Color;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import result.BufferResult;
import result.PplwareResult;
import video.Video;

/**
 * Programa com GUI responsável pela extracção dos links dos vídeos do Youtube 
 * presentes no site do Pplware (E Porque Hoje É Sexta). Todos os links extraídos
 * são guardados em disco, num ficheiro de texto. Tudo isto tirando partido
 * de Completion Services, porque se irão ter várias tarefas (tasks) que irão
 * ser submetidas num executor e se querem obter o mais rapidamente possível os 
 * resultados das mesmas assim que os mesmos estejam disponíveis e desta forma 
 * diminuir o tempo de execução de todo o programa
 * 
 * @author Marco Costa
 * @version 1.0
 * @date 12-02-2019
 *
 */

/**
 * Classe principal do programa
 */
public class PplwareSextaGUI extends Application {

	final String URL = "https://pplware.sapo.pt/humor/e-porque-hoje-e-sexta-";
	String DESTINY_PATH = "C:\\Users\\Marco\\Desktop\\_Pplware";
	String FILENAME = "Links.txt";
	int PAGINA_INICIAL_PPLWARE = 1;
	int PAGINA_FINAL_PPLWARE = 2;
	final String TIPO_VIDEO = "www.youtube.com/embed/";
	int NUM_TAREFAS = 3; //Sacar páginas, extrair links e guardar em ficheiro
	ExecutorService es;
	Thread backgroundThread;
	
	@FXML private TextField tfNumero, tfPaginaInicial, tfPaginaFinal;
	@FXML private TextField tfDestino, tfLink;
	@FXML private Button btIniciar, btPasta;
	@FXML private TextArea taInfo;
	@FXML private ProgressBar pbBarraProgresso;
	@FXML private Label lPercentagem;

	/**
	 * Método principal do programa
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		Application.launch(args);
	}
	
	/**
	 * Método invocado quando se fecha a janela do programa
	 */
	@Override
	public void stop() throws Exception {
	    super.stop();
	}
	
	/**
	 * Primeiro método a ser executado. Responsável pela criação e amostragem
	 * de toda a interface criada recorrendo a um ficheiro FXML, bem como o
	 * preenchimento de alguns campos da mesma
	 */
	@Override
	public void start(Stage stage) {
		stage.setTitle("Youtube Links Extractor: Pplware - E Porque Hoje É Sexta");
		stage.setResizable(false);
		Parent root = null;
		try {
			root = FXMLLoader.load(getClass().getResource("gui.fxml"));
		} catch (IOException e) {
			e.printStackTrace();
		}

		Scene scene = new Scene(root, 520, 340, Color.WHITE);
		stage.setScene(scene);
		
		preencheCamposGUI(root);
		validarDados(root);
		alteraDados(root);
		validaPaginas(root);
		
		stage.show();
	}

	/**
	 * Método responsável pela validação dos valores introduzidos pelo utilizador
	 * nalguns campos da GUI
	 * 
	 * @param root
	 */
	private void validarDados(Parent root) {
		((TextField) root.lookup("#numero")).textProperty()
				.addListener((observable, oldValue, newValue) -> activarBotaoIniciar(root));

		((TextField) root.lookup("#pi")).textProperty()
				.addListener((observable, oldValue, newValue) -> activarBotaoIniciar(root));

		((TextField) root.lookup("#pf")).textProperty()
				.addListener((observable, oldValue, newValue) -> activarBotaoIniciar(root));

		((TextField) root.lookup("#numero")).setTextFormatter(
				new TextFormatter<>(change -> (change.getControlNewText().matches("([1-9][0-9]*)?")) ? change : null));

		((TextField) root.lookup("#pi")).setTextFormatter(
				new TextFormatter<>(change -> (change.getControlNewText().matches("([1-9][0-9]*)?")) ? change : null));

		((TextField) root.lookup("#pf")).setTextFormatter(
				new TextFormatter<>(change -> (change.getControlNewText().matches("([1-9][0-9]*)?")) ? change : null));
	}

	/**
	 * Método que valida os valores da 'Página Inicial' e 'Página Final'. Por exemplo
	 * o valor da Página Inicial tem de ser sempre inferior ou igual à Página Final e
	 * vice-versa 
	 * 
	 * @param root
	 */
	private void validaPaginas(Parent root) {
		((TextField) root.lookup("#pi")).textProperty().addListener((observable, oldValue, newValue) -> {
			try {
				if (Integer.parseInt(((TextField) root.lookup("#pi")).getText()) <= Integer
						.parseInt(((TextField) root.lookup("#pf")).getText())) {
					((TextField) root.lookup("#pi")).setText(newValue);
				} else {
					((TextField) root.lookup("#pi")).setText(oldValue);
				}
			} catch (Exception e) {
			}
		});

		((TextField) root.lookup("#pf")).textProperty().addListener((observable, oldValue, newValue) -> {
			try {
				if (Integer.parseInt(((TextField) root.lookup("#pf")).getText()) >= Integer
						.parseInt(((TextField) root.lookup("#pi")).getText())) {
					((TextField) root.lookup("#pf")).setText(newValue);
				} else {
					((TextField) root.lookup("#pf")).setText(oldValue);
				}
			} catch (Exception e) {
			}
		});
	}

	/**
	 * Método invocado sempre que é alterado o valor da 'Página Inicial', da 
	 * 'Página Final' ou da localização da 'Pasta Destino'
	 * 
	 * @param root
	 */
	private void alteraDados(Parent root) {
		((TextField) root.lookup("#pi")).textProperty().addListener((observable, oldValue, newValue) -> actualizaValoresVariaveis(root));
		((TextField) root.lookup("#pf")).textProperty().addListener((observable, oldValue, newValue) -> actualizaValoresVariaveis(root));
		((TextField) root.lookup("#destino")).textProperty()
				.addListener((observable, oldValue, newValue) -> actualizaValoresVariaveis(root));
	}

	/**
	 * Método que actualiza os valores das variáveis correspondentes à 'Página Inicial',
	 * à 'Página Final' e ao caminho da 'Pasta Destino' sempre que as mesmas são
	 * alteradas através da GUI
	 * 
	 * @param root
	 */
	private void actualizaValoresVariaveis(Parent root) {
		try {
			PAGINA_INICIAL_PPLWARE = Integer.parseInt(((TextField) root.lookup("#pi")).getText());
			PAGINA_FINAL_PPLWARE = Integer.parseInt(((TextField) root.lookup("#pf")).getText());
			DESTINY_PATH = ((TextField) root.lookup("#destino")).getText();
		} catch (Exception e) {
		}
	}

	/**
	 * Método que preenche alguns campos na GUI do programa aquando da inicialização
	 * da mesma
	 * 
	 * @param root
	 */
	private void preencheCamposGUI(Parent root) {
		((TextField) root.lookup("#link")).setText(URL);
		((TextField) root.lookup("#destino")).setText(DESTINY_PATH);
		((TextField) root.lookup("#pi")).setText(Integer.toString(PAGINA_INICIAL_PPLWARE));
		((TextField) root.lookup("#pf")).setText(Integer.toString(PAGINA_FINAL_PPLWARE));
	}

	/**
	 * Método para permitir saber se o botão 'Iniciar' deve estar activo ou não 
	 * 
	 * @param root
	 */
	private void activarBotaoIniciar(Parent root) {
		boolean result = false;
		if (!((TextField) root.lookup("#link")).getText().equalsIgnoreCase("")
				&& !((TextField) root.lookup("#destino")).getText().equalsIgnoreCase("")
				&& !((TextField) root.lookup("#pi")).getText().equalsIgnoreCase("")
				&& !((TextField) root.lookup("#pf")).getText().equalsIgnoreCase("")
				&& !((TextField) root.lookup("#numero")).getText().equalsIgnoreCase("")) {
			if (isNumber(((TextField) root.lookup("#pi")).getText())
					&& isNumber(((TextField) root.lookup("#pf")).getText())
					&& isNumber(((TextField) root.lookup("#numero")).getText())) {
				result = true;
			}
		}
		((Button) root.lookup("#iniciar")).setDisable(!result);
	}

	/**
	 * Método invocado sempre que é activado o botão 'Pasta'
	 * 
	 * @param event
	 */
	@FXML
	protected void accaoBotaoPasta(ActionEvent event) {
		DirectoryChooser directoryChooser = new DirectoryChooser();
		File selectedDirectory = directoryChooser.showDialog(null);

		if (selectedDirectory == null) {
			// No Directory selected
		} else {
			System.out.println(selectedDirectory.getAbsolutePath());
			tfDestino.setText(selectedDirectory.getAbsolutePath());
			DESTINY_PATH = tfDestino.getText();
		}
	}
    
	/**
	 * Método invocado sempre que é activado o botão 'Iniciar'
	 * 
	 * @param event
	 */
    @SuppressWarnings("deprecation")
	@FXML
	protected void accaoBotaoIniciar(ActionEvent event) {
		if (!btIniciar.isDisable()) {
			if (btIniciar.getText().equalsIgnoreCase("Iniciar")) {
				Runnable task = () -> {

					Platform.runLater(() -> {
						tfNumero.setDisable(true);
						tfPaginaInicial.setDisable(true);
						tfPaginaFinal.setDisable(true);
						btPasta.setDisable(true);

						pbBarraProgresso.setProgress(0);
						lPercentagem.setText("0%");
					});

					PAGINA_INICIAL_PPLWARE = Integer.parseInt(tfPaginaInicial.getText());
					PAGINA_FINAL_PPLWARE = Integer.parseInt(tfPaginaFinal.getText());
					DESTINY_PATH = tfDestino.getText();

					Platform.runLater(() -> {
						btIniciar.setText("Cancelar");
						taInfo.appendText("[INFO]: Processo Iniciado..." + "\n");
					});

					final long inicio = System.currentTimeMillis();
					es = Executors.newCachedThreadPool();
					String numPost = null;
					numPost = tfNumero.getText();

					Hashtable<String, Video> h = null;

					if (isNumber(numPost)) {
						h = new Hashtable<String, Video>();

						try {
							obterLinksPplware(numPost, h);
						} catch (Exception e) {
						}

					} else {
						System.out.println("Vossemecê não inseriu um número!!!");
					}

					es.shutdown();

					try {
						es.awaitTermination(1, TimeUnit.HOURS);
					} catch (Exception e) {
						System.err.println(e);
						Platform.runLater(() -> taInfo.appendText("[ERRO]: " + e + "\n"));
					}

					guardaDadosEmFicheiro(DESTINY_PATH.concat("\\").concat(FILENAME), h);

					final long fim = System.currentTimeMillis();
					System.out.println();
					System.out.println("Tempo de execução do Programa: " + tempoFormatoAmigavel((fim - inicio)));
					
					Platform.runLater(() -> {
						taInfo.appendText("[INFO]: Tempo de execução do Programa: "
								+ tempoFormatoAmigavel((fim - inicio)) + "\n");
						btIniciar.setText("Iniciar");

						tfNumero.setDisable(false);
						tfPaginaInicial.setDisable(false);
						tfPaginaFinal.setDisable(false);
						btPasta.setDisable(false);
					});
				};
				
				backgroundThread = new Thread(task);

				// Terminate the running thread if the application exits
				backgroundThread.setDaemon(true);

				// Start the thread
				backgroundThread.start();
				
			} else if (btIniciar.getText().equalsIgnoreCase("Cancelar")) {
				es.shutdown();
				backgroundThread.stop();
				
				Platform.runLater(() -> {
					tfNumero.setDisable(false);
					tfPaginaInicial.setDisable(false);
					tfPaginaFinal.setDisable(false);
					btPasta.setDisable(false);

					btIniciar.setText("Iniciar");
					taInfo.appendText("[INFO]: Processo Cancelado!" + "\n");
					pbBarraProgresso.setProgress(0);
					lPercentagem.setText("0%");
				});
			}
		}
	}

	/**
	 * Método para obter os links dos vídeos do Youtube no site do Pplware
	 * 
	 * @param argumento
	 * @param h
	 */
	private void obterLinksPplware(String argumento, Hashtable<String, Video> h) {
		System.out.println("A iniciar processamento...");

		CompletionService<Buffer> cs = new ExecutorCompletionService<Buffer>(es);
		CompletionService<Hashtable<String, Video>> cs2 = new ExecutorCompletionService<Hashtable<String, Video>>(es);

		for (int i = PAGINA_INICIAL_PPLWARE; i <= PAGINA_FINAL_PPLWARE; i++) {
			String tmp = URL.concat(argumento).concat("/").concat(Integer.toString(i));
			cs.submit(new BufferResult(tmp));
		}

		for (int i = PAGINA_INICIAL_PPLWARE; i <= PAGINA_FINAL_PPLWARE; i++) {
			Buffer b = null;
			try {
				b = cs.take().get();
				cs2.submit(new PplwareResult(b.getBr()));

			} catch (InterruptedException | ExecutionException e) {
				System.err.println(e);
				Platform.runLater(() -> taInfo.appendText("[ERRO]: " + e + "\n"));
			}

			if (b != null) {
				int calcPercentagem = i * 100 / PAGINA_FINAL_PPLWARE;
				System.out.print("\r");
				System.out.print(obtemMensagem(1, calcPercentagem));

				String mensagem = "[INFO]: " + obtemMensagem(1, calcPercentagem);

				Platform.runLater(() -> taInfo.appendText(mensagem));
				if (calcPercentagem < 100)
					Platform.runLater(
							() -> taInfo.deleteText(taInfo.getLength() - mensagem.length(), taInfo.getLength()));

				Platform.runLater(() -> {
					pbBarraProgresso.setProgress(calcPercentagem * 1 / NUM_TAREFAS);
					lPercentagem.setText(Integer.toString(calcPercentagem * 1 / NUM_TAREFAS).concat("%"));
				});
			}

			if (b == null && i == PAGINA_FINAL_PPLWARE)
				return;
		}

		System.out.println();
		Platform.runLater(() -> taInfo.appendText("\n"));
		for (int i = PAGINA_INICIAL_PPLWARE; i <= PAGINA_FINAL_PPLWARE; i++) {
			Hashtable<String, Video> hashTmp = new Hashtable<String, Video>();
			try {
				hashTmp = cs2.take().get();
				Enumeration<String> en = hashTmp.keys();
				while (en.hasMoreElements()) {
					String chave = (String) en.nextElement();
					Video vid = (Video) hashTmp.get(chave);
					if (h.get(chave) == null) {
						h.put(chave, vid);
					}
				}
			} catch (InterruptedException | ExecutionException e) {
				System.err.println(e);
				Platform.runLater(() -> taInfo.appendText("[ERRO]: " + e + "\n"));
			}

			int calcPercentagem = i * 100 / PAGINA_FINAL_PPLWARE;
			System.out.print("\r");
			System.out.print(obtemMensagem(2, calcPercentagem));

			String mensagem = "[INFO]: " + obtemMensagem(2, calcPercentagem);

			Platform.runLater(() -> taInfo.appendText(mensagem));
			if (calcPercentagem < 100)
				Platform.runLater(() -> taInfo.deleteText(taInfo.getLength() - mensagem.length(), taInfo.getLength()));

			Platform.runLater(() -> {
				pbBarraProgresso.setProgress(calcPercentagem * 2 / NUM_TAREFAS);
				lPercentagem.setText(Integer.toString(calcPercentagem * 2 / NUM_TAREFAS).concat("%"));
			});
		}
	}

	/**
	 * Método para verificar se uma String é um número
	 * 
	 * @param s
	 * @return true se for um número ou false caso contrário
	 */
	private boolean isNumber(String s) {
		boolean result = false;
		try {
			Integer.parseInt(s);
			result = true;
		} catch (Exception e) {
			result = false;
		}
		return result;
	}

	/**
	 * Método para obter o link de um vídeo do Youtube numa String
	 * 
	 * @param src
	 * @return uma string com o link do vídeo ou "" caso contrário
	 */
	public String videosYoutube(String src) {
		String result = "";
		String limitador = TIPO_VIDEO;
		if (src.contains(limitador)) {
			result = src.substring(src.indexOf(limitador), src.indexOf(limitador) + limitador.length() + 11);
		}
		return result;
	}
	
	/**
	 * Método para alterar o URL do vídeo de 'www.youtube.com/embed/' para 
	 * 'www.youtube.com/watch?v='
	 *  
	 * @param st
	 * @return URL do vídeo modificado
	 */
	private String alteraURLVideo(String st) {
		String novoURL = "www.youtube.com/watch?v=";
		st = st.replace(TIPO_VIDEO, novoURL);
		return st;
	}

	/**
	 * Método para guardar os dados dos vídeos num ficheiro de texto
	 * 
	 * @param filename
	 * @param h
	 */
	private void guardaDadosEmFicheiro(String filename, Hashtable<String, Video> h) {
		if (h != null) {
			if (h.size() > 0) {
				System.out.println();
				Platform.runLater(() -> taInfo.appendText("\n"));
				int i = 0;
				try {
					FileOutputStream fos = new FileOutputStream(filename);
					OutputStreamWriter osw = new OutputStreamWriter(fos, StandardCharsets.UTF_8);
					BufferedWriter writer = new BufferedWriter(osw);
					Enumeration<String> en = h.keys();
					while (en.hasMoreElements()) {
						i++;
						String chave = (String) en.nextElement();
						writer.write(alteraURLVideo(chave));
						writer.newLine();

						int calcPercentagem = i * 100 / h.size();
						System.out.print("\r");
						System.out.print(obtemMensagem(5, calcPercentagem));

						String mensagem = "[INFO]: " + obtemMensagem(5, calcPercentagem);

						Platform.runLater(() -> taInfo.appendText(mensagem));
						if (calcPercentagem < 100)
							Platform.runLater(() -> taInfo.deleteText(taInfo.getLength() - mensagem.length(),
									taInfo.getLength()));

						Platform.runLater(() -> {
							pbBarraProgresso.setProgress(calcPercentagem * 3 / NUM_TAREFAS);
							lPercentagem.setText(Integer.toString(calcPercentagem * 3 / NUM_TAREFAS).concat("%"));
						});
					}
					System.out.println();
					System.out.println("Número total de Links: " + h.size());

					Platform.runLater(() -> taInfo.appendText("\n[INFO]: Número total de Links: " + h.size() + "\n"));

					writer.close();
					osw.close();
					fos.close();
				} catch (Exception e) {
					System.err.println(e);
					Platform.runLater(() -> taInfo.appendText("[ERRO]: " + e + "\n"));
				}
			}
		}
	}

	/**
	 * Método para devolver o tempo de execução do programa num formato amigável
	 * 
	 * @param t
	 * @return String em formato amigável com o tempo de execução do programa
	 */
	private static String tempoFormatoAmigavel(long t) {
		String result = "";
		long ms = 0, s = 0, min = 0, h = 0;
		int i = 1;
		String milissegundos = "", segundos = "", minutos = "", horas = "";
		while (t > 0) {
			switch (i) {
				case 1:
					ms = t % 1000;
					t = t / 1000;
					milissegundos = milissegundos.concat(Long.toString(ms)).concat("ms");
					i++;
					break;
				case 2:
					s = t % 60;
					t = t / 60;
					segundos = segundos.concat(Long.toString(s)).concat("s ");
					i++;
					break;
				case 3:
					min = t % (60 * 60);
					t = t / (60 * 60);
					minutos = minutos.concat(Long.toString(min)).concat("min ");
					i++;
					break;
				case 4:
					h = t % (24 * 60 * 60);
					t = t / (24 * 60 * 60);
					horas = horas.concat(Long.toString(h)).concat("h ");
					i++;
					break;
			}
		}
		result = horas.concat(minutos).concat(segundos).concat(milissegundos);
		return result;
	}

	/**
	 * Método que devolve uma mensagem a indicar o que o programa está a fazer e
	 * em que estado está, dependendo dos parâmetros de entrada (id e
	 * percentagem) que recebe
	 * 
	 * @param id
	 * @param percentagem
	 * @return String com mensagem a indicar o que o programa está a fazer e em
	 *         que estado está
	 */
	private static String obtemMensagem(int id, int percentagem) {
		String s = "";
		switch (id) {
			case 1: s = "A obter páginas do Pplware..."; break;
			case 2: s = "A obter links do Youtube..."; break;
			case 3: s = "A obter páginas de Download..."; break;
			case 4: s = "A obter links de Download..."; break;
			case 5: s = "A guardar dados em Disco..."; break;
		}
		s = s.concat(" ").concat(Integer.toString(percentagem)).concat("%");
		return s;
	}
}