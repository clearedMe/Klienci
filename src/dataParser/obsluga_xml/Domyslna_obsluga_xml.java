package dataParser.obsluga_xml;

import java.util.LinkedList;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import dataParser.Zapis_do_bazy_analizatora_klienta;
import dataParser.Kontakt;

public class Domyslna_obsluga_xml extends DefaultHandler
{
	private Zapis_do_bazy_analizatora_klienta interfejs_bazy;
	
	private String imie;
	private String nazwisko;
	private String wiek;
	private String miasto;
	private List<Kontakt> kontakty;
	
	private String tekst;
	private boolean analiza_kontaktow;
	
	public Domyslna_obsluga_xml(Zapis_do_bazy_analizatora_klienta n_interfejs_bazy)
	{
		this.interfejs_bazy = n_interfejs_bazy;
		
		this.wiek = null;
		this.analiza_kontaktow = false;
		this.kontakty = new LinkedList<Kontakt>();
	}
	
	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException
	{
		if (qName.equals("contacts"))
			this.analiza_kontaktow = true;
	}

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException
	{
		if (qName.equals("person"))
		{
			this.interfejs_bazy.zapisz_rekord_do_bazy(imie,nazwisko,wiek,miasto,kontakty);
			
			this.wiek = null;
			kontakty.clear();
		} else
		if (qName.equals("contacts"))
		{
			this.analiza_kontaktow = false;	
		}
		
		if (analiza_kontaktow == true)
		{
			if (qName.equals("phone"))
				this.kontakty.add(new Kontakt(this.tekst,Kontakt.s_typ_telefon)); else
			if (qName.equals("email"))
				this.kontakty.add(new Kontakt(this.tekst,Kontakt.s_typ_email)); else
			if (qName.equals("jabber"))
				this.kontakty.add(new Kontakt(this.tekst,Kontakt.s_typ_jabber)); else
				this.kontakty.add(new Kontakt(this.tekst,Kontakt.s_typ_nieznany));				
		}
		
		if (qName.equals("name"))
			this.imie = this.tekst; else
		if (qName.equals("surname"))
			this.nazwisko = this.tekst; else
		if (qName.equals("age"))
			this.wiek = this.tekst; else
		if (qName.equals("city"))
			this.miasto = this.tekst;
	}

	@Override
	public void characters(char[] ch, int start, int length) throws SAXException
	{
		this.tekst = String.copyValueOf(ch,start,length).trim();
	}
}
