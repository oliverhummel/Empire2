package net.zehnkampf.empire.domain.tokens;

import java.util.ArrayList;

public abstract class TransporterToken extends Token {
	final protected ArrayList<Token> tokens = new ArrayList<Token>();

	public TransporterToken() {
		this(-1, -1);
	}

	public TransporterToken(int x, int y) {
		super(x, y);
	}

	public boolean move() throws ImpossibleMoveException, WaitForLoadException {
		boolean result = super.move();

		for (Token token : tokens) {
			token.setX(x);
			token.setY(y);
		}

		return result;
	}

	public boolean loadIsCurrentToken() {
		for (Token token : tokens) {
			if (token.isCurrent())
				return true;
		}

		return false;
	}
	
    public boolean isLoaded() {
    	return tokens.size() > 0;
    }
    
	public void addToken(Token token) {
		tokens.add(token);
		increaseCount(token);
		token.setLoad(true);
		token.setTarget(-1,-1);
	}

	public void removeToken(Token token) {
		tokens.remove(token);
		decreaseCount(token);
		token.setLoad(false);
	}
	
	public boolean hasLoaded(Token token) {
		return tokens.contains(token);
	}
	
	public ArrayList<Token> getTokens() {
		return tokens;
	}

	public abstract void increaseCount(Token token);
	public abstract void decreaseCount(Token token);
}  // eof
