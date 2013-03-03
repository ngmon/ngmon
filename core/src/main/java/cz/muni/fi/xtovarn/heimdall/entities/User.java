package cz.muni.fi.xtovarn.heimdall.entities;

public class User {

	private String login;
	private String passcode;
	
	public User() {
	}

	public User(String login, String passcode) {
		this.login = login;
		this.passcode = passcode;
	}

	public String getLogin() {
		return login;
	}

	public void setLogin(String login) {
		this.login = login;
	}

	public String getPasscode() {
		return passcode;
	}

	public void setPasscode(String passcode) {
		this.passcode = passcode;
	}

}
