package dataParser;

public class Kontakt
{
	public static final int s_typ_nieznany = 0;
	public static final int s_typ_email = 1;
	public static final int s_typ_telefon = 2;
	public static final int s_typ_jabber = 3;
	
	
	private String wartosc;
	private int typ;
	
	public Kontakt(String n_wartosc, int n_typ)
	{
		this.wartosc = n_wartosc;
		this.typ = n_typ;
	}
	
	public String zwroc_wartosc()
	{
		return this.wartosc;
	}
	public int zwroc_typ()
	{
		return this.typ;
	}
}
