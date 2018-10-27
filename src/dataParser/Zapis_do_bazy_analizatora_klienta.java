package dataParser;

import java.util.List;

public interface Zapis_do_bazy_analizatora_klienta
{
	void zapisz_rekord_do_bazy(String imie, String nazwisko, String wiek, String miasto, List<Kontakt> kontakty);
}
