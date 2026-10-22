package video;
/**
 * Classe responsável pela definição de um vídeo
 *
 * @author Marco Costa
 * @version 1.0
 * @date 15-04-2018
 *
 */
public class Video {
	String linkYoutube;
	String titulo;
	String linkDownload;

	/**
	 * Construtor da classe
	 * 
	 * @param linkYoutube
	 * @param titulo
	 * @param linkDownload
	 */
	public Video(String linkYoutube, String titulo, String linkDownload) {
		this.linkYoutube = linkYoutube;
		this.titulo = titulo;
		this.linkDownload = linkDownload;
	}

	/**
	 * Método que devolve o link do vídeo no Youtube
	 * 
	 * @return link do vídeo no Youtube
	 */
	public String getLinkYoutube() {
		return this.linkYoutube;
	}

	/**
	 * Método que devolve o título do vídeo
	 * 
	 * @return título do vídeo
	 */
	public String getTitulo() {
		return this.titulo;
	}

	/**
	 * Método que devolve o link de download do vídeo
	 * 
	 * @return link de download do vídeo
	 */
	public String getLinkDownload() {
		return this.linkDownload;
	}

	/**
	 * Método para alterar o link do vídeo no Youtube
	 * 
	 * @param linkYoutube
	 */
	public void setLinkYoutube(String linkYoutube) {
		this.linkYoutube = linkYoutube;
	}

	/**
	 * Método para alterar o título do vídeo
	 * 
	 * @param titulo
	 */
	public void setTitulo(String titulo) {
		this.titulo = titulo;
	}

	/**
	 * Método para alterar o link de download do vídeo
	 * 
	 * @param linkDownload
	 */
	public void setLinkDownload(String linkDownload) {
		this.linkDownload = linkDownload;
	}

	/**
	 * Método que devolve uma String com todos os dados do vídeo
	 * 
	 * @return String com todos os dados do vídeo
	 */
	@Override
	public String toString() {
		return this.linkYoutube.concat("\t").concat(this.titulo).concat("\t").concat(this.linkDownload);
	}
}