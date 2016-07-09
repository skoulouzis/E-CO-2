package eu.edisonproject.classification.prepare.model;

import org.jsoup.Jsoup;
import org.jsoup.examples.HtmlToPlainText;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
/*
 * @author Michele Sparamonti
 */
public class Text extends Extractor{

	public void extract() {
		Document d = Jsoup.parse(this.getJp().getDescription());
		Element e = d.body();
		HtmlToPlainText h2p = new HtmlToPlainText();
		this.getJp().setDescription(h2p.getPlainText(e));
	}

}
