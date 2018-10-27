package dataParser;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.annotation.Annotation;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import dataParser.obsluga_xml.Domyslna_obsluga_xml;

public class Analizator_klienta implements Analizator_plikow, Zapis_do_bazy_analizatora_klienta
{			
	public static Analizator_plikow utworz(Connection n_baza)
	{
		return new Analizator_klienta(n_baza);
	}
	
	
	private Connection baza;

	private Analizator_klienta(Connection n_baza)
	{
		this.baza = n_baza;
	}
	
	public void wprowadz_plik_do_bazy(String nazwa_pliku)
	{
		try
		{
			if (nazwa_pliku.contains(".xml"))
				this.analizuj_xml(nazwa_pliku); else
				this.analizuj_csv(nazwa_pliku);					

		} catch (FileNotFoundException e)
		{
			System.out.println("Podany plik nie istnieje: "+nazwa_pliku);
		} catch (IOException e)
		{
			System.out.println("Nieprawidlowe dane w pliku: "+nazwa_pliku);
		} catch (SQLException e)
		{
			System.out.println("Blad zapisu do bazy danych dla pliku: " + nazwa_pliku);
		}	
	}
	
	private void analizuj_xml(String nazwa_pliku) throws IOException, SQLException
	{
		try
		{
			SAXParserFactory fabryka = SAXParserFactory.newInstance();
			SAXParser budowniczy = fabryka.newSAXParser();
			
			DefaultHandler obsluga_xml = new Domyslna_obsluga_xml(this);
			
			budowniczy.parse(new InputSource(new InputStreamReader(
					new FileInputStream(nazwa_pliku),StandardCharsets.UTF_8)),obsluga_xml);

		} catch (ParserConfigurationException e)
		{
			e.printStackTrace();
		} catch (SAXException e)
		{
			e.printStackTrace();
		}
	}
	
	private void analizuj_csv(String nazwa_pliku) throws IOException, SQLException
	{
		BufferedReader plik = new BufferedReader(
				new InputStreamReader(new FileInputStream(nazwa_pliku),StandardCharsets.UTF_8));
		
		String linia;
		List<Kontakt> kontakty = new LinkedList<Kontakt>();
		
		while ((linia = plik.readLine()) != null)
		{
			Scanner skaner = new Scanner(linia);
			skaner.useDelimiter(",");
			
			String imie = skaner.next();
			String nazwisko = skaner.next();
			String wiek = skaner.next();
			String miasto = skaner.next();
			
			while (skaner.hasNext() == true)
			{
				String wartosc = skaner.next();
				kontakty.add(new Kontakt(wartosc,this.zwroc_typ_kontaktu(wartosc)));
			}
			
			this.zapisz_rekord_do_bazy(imie,nazwisko,wiek,miasto,kontakty);
			
			kontakty.clear();
			skaner.close();
		}
		
		plik.close();
	}
	
	public void zapisz_rekord_do_bazy(String imie, String nazwisko, String wiek, String miasto, List<Kontakt> kontakty)
	{
		try
		{
			long id_klienta = this.zapisz_klienta_do_bazy(imie,nazwisko,wiek,miasto);
			this.zapisz_kontakty_do_bazy(id_klienta,kontakty);
			
		} catch (SQLException e)
		{
			System.out.println("Nieudany zapis do bazy");
		}
	}
	
	private long zapisz_klienta_do_bazy(String imie, String nazwisko, String wiek, String miasto) throws SQLException
	{
		String zapytanie_sql = "INSERT INTO CUSTOMERS VALUES (NAME=?,SURNAME=?,AGE=?)";
		PreparedStatement zapytanie = this.baza.prepareStatement(zapytanie_sql,Statement.RETURN_GENERATED_KEYS);
		zapytanie.setString(1,imie);
		zapytanie.setString(2,nazwisko);
		
		if (wiek.isEmpty())
			zapytanie.setNull(3,java.sql.Types.INTEGER); else
			zapytanie.setInt(3,Integer.parseInt(wiek));
		
		zapytanie.executeQuery();
		
		ResultSet wynik = zapytanie.getGeneratedKeys();
		wynik.next();
		return wynik.getLong(1);
	}
	private void zapisz_kontakty_do_bazy(long id_klienta, List<Kontakt> kontakty) throws SQLException
	{
		for (Kontakt kontakt : kontakty)
		{
			int typ = kontakt.zwroc_typ();
			
			String zapytanie_sql = "INSERT INTO CONTACTS VALUES (ID_CUSTOMER=?,TYPE=?,CONTACT=?)";
			PreparedStatement zapytanie = this.baza.prepareStatement(zapytanie_sql);
			zapytanie.setLong(1,id_klienta);
			zapytanie.setInt(2,typ);
			zapytanie.setString(3,kontakt.zwroc_wartosc());
			
			zapytanie.executeQuery();
		}
	}
	
	private int zwroc_typ_kontaktu(String kontakt)
	{
		int typ = 0;
		
		if (kontakt.contains("@"))
			typ = 1; else
		if (kontakt.trim().replace("-","").matches("[0-9]{9}"))
			typ = 2; else
		if (kontakt.matches("[a-zA-Z0-9]+"))
			typ = 3;
		
		return typ;
	}
}

// https://github.com/clearedMe/Klienci.git