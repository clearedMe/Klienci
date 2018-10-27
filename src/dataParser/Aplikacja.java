package dataParser;

import java.sql.Connection;
import java.sql.DriverManager;

public class Aplikacja
{
	public static void main(String[] args)
	{
		args = new String[]{"url_bazy","uzytkownik","haslo","dane/dane-osoby.txt","dane/dane-osoby.xml"};

		try
		{
			Connection baza = DriverManager.getConnection(args[0],args[1],args[2]);
			
			Analizator_plikow analizator = Analizator_klienta.utworz(baza);		
			for (int i = 3; i < args.length; i++)
			{
				String nazwa_pliku = args[i];
				analizator.wprowadz_plik_do_bazy(nazwa_pliku);
			}
			
			baza.close();
			
		} catch (Exception wyjatek)
		{
			System.out.println("Nieudane polaczenie z baza! (sprawdz czy url, nazwa uzytkownika i haslo sa prawidlowe)");			
		}
	}
}