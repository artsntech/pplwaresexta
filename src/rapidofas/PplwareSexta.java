package rapidofas;
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
import result.BufferResult;
//import result.DownloadResult;
import result.PplwareResult;
import video.Video;

/**
 * Programa responsável pela extracção dos links dos vídeos do Youtube presentes
 * no site do Pplware (E Porque Hoje É Sexta), obtenção dos seus títulos e dos
 * respectivos links de download no site do PickVideo. No final, vão-se guardar
 * todos estes dados em disco, num ficheiro de texto. Tudo isto tirando partido
 * de Completion Services, porque se irão ter várias tarefas (tasks) que irão
 * ser submetidas num executor e se querem obter o mais rapidamente possível os 
 * resultados das mesmas assim que os mesmos estejam disponíveis e desta forma 
 * diminuir o tempo de execução de todo o programa
 * 
 * @author Marco Costa
 * @version 1.0
 * @date 24-04-2018
 *
 */
public class PplwareSexta {

	final static String URL = "https://pplware.sapo.pt/humor/e-porque-hoje-e-sexta-";
	final static String FILENAME = "C:\\Users\\Marco\\Desktop\\_Pplware\\Links.txt";
	final static String URL_DOWNLOAD_VIDEO = "https://pickvideo.net/download?video=";
	final static int PAGINA_INICIAL_PPLWARE = 1;
	final static int PAGINA_FINAL_PPLWARE = 2;
	final static String QUALIDADE_VIDEO = "144p";
	final static String EXTENSAO_VIDEO = "3gp";
	final static String TIPO_VIDEO = "www.youtube.com/embed/";
	static ExecutorService es;

	/**
	 * Método principal do programa
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		long inicio = 0, fim = 0;
		inicio = System.currentTimeMillis();
		es = Executors.newCachedThreadPool();
		Hashtable<String, Video> h = null;

		if (args.length > 0) {
			if (isNumber(args[0])) {
				h = new Hashtable<String, Video>();

				obterLinksPplware(args[0], h);

			} else {
				System.out.println("Vossemecê não inseriu um número!!!");
			}
		} else {
			System.out.println("E o argumento?!!");
		}
		
		es.shutdown();
		
		try {
			es.awaitTermination(1, TimeUnit.HOURS);
		} catch(Exception e) {
			System.err.println(e);
		}
		
		guardaDadosEmFicheiro(FILENAME, h);
		
		fim = System.currentTimeMillis();
		System.out.println();
		System.out.println("Tempo de execução do Programa: " + tempoFormatoAmigavel((fim - inicio)));
	}

	/**
	 * Método para obter os links dos vídeos do Youtube no site do Pplware
	 * 
	 * @param argumento
	 * @param h
	 */
	private static void obterLinksPplware(String argumento, Hashtable<String, Video> h) {
		System.out.println("A iniciar processamento...");
		
		CompletionService<Buffer> cs = new ExecutorCompletionService<Buffer>(es);
		CompletionService<Hashtable<String, Video>> cs2 = new ExecutorCompletionService<Hashtable<String, Video>>(es);
		//CompletionService<Buffer> cs3 = new ExecutorCompletionService<Buffer>(es);
		
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
			}
	
			if(b != null) {
				int calcPercentagem = i * 100 / PAGINA_FINAL_PPLWARE;
				System.out.print("\r");
				System.out.print(obtemMensagem(1, calcPercentagem));
			}
			
			if(b == null && i == PAGINA_FINAL_PPLWARE)
				return;
		}
		
		System.out.println();
		for (int i = PAGINA_INICIAL_PPLWARE; i <= PAGINA_FINAL_PPLWARE; i++) {
			Hashtable<String, Video> hashTmp = new Hashtable<String, Video>();
			try {
				hashTmp = cs2.take().get();
				Enumeration<String> en = hashTmp.keys();
				while (en.hasMoreElements()) {
					String chave = (String) en.nextElement();
					Video vid = (Video) hashTmp.get(chave);
					if(h.get(chave)==null) {
						h.put(chave, vid);
						//String tmp = URL_DOWNLOAD_VIDEO.concat("http://").concat(chave);
						//cs3.submit(new BufferResult(tmp));
					}
				}
			} catch (InterruptedException | ExecutionException e) {
				System.err.println(e);
			}
			
			int calcPercentagem = i * 100 / PAGINA_FINAL_PPLWARE;
			System.out.print("\r");
			System.out.print(obtemMensagem(2, calcPercentagem));
		}
		
		//obterLinksDownload(h, cs3);
	}

	/**
	 * Método para obter os links de download dos vídeos do Youtube no PickVideo
	 * 
	 * @param h
	 * @param cs
	 */
	/*private static void obterLinksDownload(Hashtable<String, Video> h, CompletionService<Buffer> cs) {
		CompletionService<Video> cs2 = new ExecutorCompletionService<Video>(es);
		
		if (h.size() > 0) {
			System.out.println();
			
			for(int i = 1; i<=h.size(); i++) {
				try {
					Buffer b = cs.take().get();					
					cs2.submit(new DownloadResult(b.getBr(), h, b.getStr()));
					
				} catch (InterruptedException | ExecutionException e) {
					System.err.println(e);
				}
				
				int calcPercentagem = i * 100 / h.size();
				System.out.print("\r");
				System.out.print(obtemMensagem(3, calcPercentagem));
			}
			
			System.out.println();
			for(int j = 1; j<=h.size(); j++) {
				try {
					Video v = cs2.take().get();
					if(h.get(v.getLinkYoutube())!=null) {
						Video v2 = (Video) h.get(v.getLinkYoutube());
						v2.setTitulo(v.getTitulo());
						v2.setLinkDownload(v.getLinkDownload());
						h.replace(v2.getLinkYoutube(), v2);
					}
				} catch (InterruptedException | ExecutionException e) {
					System.err.println(e);
				}
				
				int calcPercentagem = j * 100 / h.size();
				System.out.print("\r");
				System.out.print(obtemMensagem(4, calcPercentagem));
			}
		}
	}*/

	/**
	 * Método para verificar se uma String é um número
	 * 
	 * @param s
	 * @return true se for um número ou false caso contrário
	 */
	private static boolean isNumber(String s) {
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
	public static String videosYoutube(String src) {
		String result = "";
		String limitador = TIPO_VIDEO;
		if (src.contains(limitador)) {
			result = src.substring(src.indexOf(limitador), src.indexOf(limitador) + limitador.length() + 11);
		}
		return result;
	}

	/**
	 * Método para verificar se uma String contém QUALIDADE_VIDEO
	 * 
	 * @param src
	 * @return true se a qualidade do vídeo for igual a QUALIDADE_VIDEO ou false
	 *         caso contrário
	 */
	public static boolean qualidade(String src) {
		boolean result = false;
		String qualidade = QUALIDADE_VIDEO;
		if (src.contains(qualidade)) {
			result = true;
		}
		return result;
	}

	/**
	 * Método para verificar se uma String contém EXTENSAO_VIDEO
	 * 
	 * @param src
	 * @return true se a extensão do vídeo for igual a EXTENSAO_VIDEO ou false
	 *         caso contrário
	 */
	public static boolean extensao(String src) {
		boolean result = false;
		String extensao = EXTENSAO_VIDEO;
		if (src.contains(extensao)) {
			result = true;
		}
		return result;
	}

	/**
	 * Método para obter o link de download de um vídeo
	 * 
	 * @param src
	 * @param qualidade
	 * @param extensao
	 * @return String com o link de download do vídeo ou "" caso contrário
	 */
	public static String linkDownload(String src, boolean qualidade, boolean extensao) {
		String result = "";
		String limitadorInicio = "href=\"";
		String limitadorFim = "\">Download";
		if (qualidade && extensao) {
			if (src.contains(limitadorInicio)) {
				result = src.substring(src.indexOf(limitadorInicio) + limitadorInicio.length(),
						src.indexOf(limitadorFim));
			}
		}
		return result;
	}

	/**
	 * Método para obter o título de um vídeo
	 * 
	 * @param src
	 * @param qualidade
	 * @param extensao
	 * @return String com o título do vídeo ou "" caso contrário
	 */
	public static String tituloVideo(String src, boolean qualidade, boolean extensao) {
		String result = "";
		String limitadorInicio = "download=\"";
		String limitadorFim = ".".concat(EXTENSAO_VIDEO).concat("\"");
		if (qualidade && extensao) {
			if (src.contains(limitadorInicio)) {
				result = src.substring(src.indexOf(limitadorInicio) + limitadorInicio.length(),
						src.indexOf(limitadorFim) + limitadorFim.length() - 1);
			}
		}
		return result;
	}

	/**
	 * Método para alterar os caracteres inválidos por caracteres válidos do
	 * título de um vídeo
	 * 
	 * @param src
	 * @return String com os caracteres inválidos substituídos para caracteres
	 *         válidos
	 */
	public static String alteraTitulo(String src) {
		String[] caracteresInvalidos = { "\\", "/", ":", "*", "?", "\"", "<", ">", "|" };
		String[] caracteresSubstitutos = { "-", "-", "-", "#", "7", "'", "«", "»", "-" };
		src = substituiCaracteresHtmlEspeciais(src);
		for (int i = 0; i < caracteresInvalidos.length; i++) {
			if (src.contains(caracteresInvalidos[i])) {
				src = src.replace(caracteresInvalidos[i], caracteresSubstitutos[i]);
			}
		}
		return src;
	}

	/**
	 * Método para substituir os caracteres HTML especiais pelos caracteres
	 * equivalentes
	 * 
	 * @param src
	 * @return String com os caracteres HTML especiais substituídos pelos seus
	 *         equivalentes
	 */
	private static String substituiCaracteresHtmlEspeciais(String src) {
		String[] caracteresHtmlEspeciais = { "&lt;", "&gt;", "&amp;", "&quot;", "&#009;", "&#033;", "&#035;", "&#036;",
				"&#037;", "&#039;", "&#040;", "&#041;", "&#042;", "&#043;", "&#044;", "&#045;", "&#046;", "&#047;",
				"&#058;", "&#059;", "&#061;", "&#063;", "&#064;", "&#091;", "&#092;", "&#093;", "&#094;", "&#095;",
				"&#096;", "&#123;", "&#124;", "&#125;", "&#126;" };
		String[] caracteresSubstitutos = { "<", ">", "&", "\"", "\\t", "!", "#", "$", "%", "'", "(", ")", "*", "+", ",",
				"-", ".", "/", ";", ":", "=", "?", "@", "[", "\\", "]", "^", "_", "`", "{", "|", "}", "~" };
		for (int i = 0; i < caracteresHtmlEspeciais.length; i++) {
			if (src.contains(caracteresHtmlEspeciais[i])) {
				src = src.replace(caracteresHtmlEspeciais[i], caracteresSubstitutos[i]);
			}
		}
		return src;
	}
	
	/**
	 * Método para alterar o URL do vídeo de 'www.youtube.com/embed/' para 
	 * 'www.youtube.com/watch?v='
	 *  
	 * @param st
	 * @return URL do vídeo modificado
	 */
	private static String alteraURLVideo(String st) {
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
	private static void guardaDadosEmFicheiro(String filename, Hashtable<String, Video> h) {
		if(h!=null) {
			if (h.size() > 0) {
				System.out.println();
				int i = 0;
				try {
					FileOutputStream fos = new FileOutputStream(filename);
					OutputStreamWriter osw = new OutputStreamWriter(fos, StandardCharsets.UTF_8);
					BufferedWriter writer = new BufferedWriter(osw);
					Enumeration<String> en = h.keys();
					while (en.hasMoreElements()) {
						i++;
						String chave = (String) en.nextElement();
						//Video vid = (Video) h.get(chave);
						//ATENÇÃO: em vez da chave estava vid.toString()
						writer.write(alteraURLVideo(chave));
						writer.newLine();
	
						int calcPercentagem = i * 100 / h.size();
						System.out.print("\r");
						System.out.print(obtemMensagem(5, calcPercentagem));
					}
					System.out.println();
					System.out.println("Número total de Links: " + h.size());
					writer.close();
					osw.close();
					fos.close();
				} catch (Exception e) {
					System.err.println(e);
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