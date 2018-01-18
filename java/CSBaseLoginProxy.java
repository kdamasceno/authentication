package csbase.util.proxy;

import csbase.logic.SingleServerManager;
import csbase.logic.ServerURI;
import csbase.logic.Session;
import csbase.logic.User;

import csbase.remote.ServerEntryPoint;
import csbase.remote.ClientRemoteLocator;

import csbase.exception.CSBaseException;

import java.util.Locale;
import java.rmi.RemoteException;

/**
 * Classe que atua como proxy de login a um servidor CSBase para fins de autenticação.
 */
public class CSBaseLoginProxy {
    /** host do servidor csbase */
    private String host;

    /** porta onde está rodando o servidor CSBase */
    private int port;

    /** indica se o lookup do servidor deve ser verbose */
    private boolean verboseLookup;

    /** referência para o gerenciador do servidor a partir do qual se obtem o entryPoint e se autentica */
    private BasicServerManager sManager;

    /**
     * Construtor
     * @param host máquina onde está o csbase
     * @param port porta onde está rodando o csbase naquele host
     * @param verboseLookup se o lookup do servidor deve ser verbose ou não
     */
    public CSBaseLoginProxy(String host, int port, boolean verboseLookup) {
	this.host = host;
	this.port = port;
	this.verboseLookup = verboseLookup;
	init();
    }

    /**
     * Construtor
     */
    public CSBaseLoginProxy(String host, String port, boolean verboseLookup) {
	this(host, Integer.parseInt(port), verboseLookup);
    }

    /**
     * Faz a inicialização: lookup do servidor e obtenção do serverManager
     */
    private void init() {
	ServerURI serverURI = ServerURI.create(host, port);
	sManager = new BasicServerManager(serverURI, ClientRemoteLocator.class, verboseLookup);
	try {    
	    sManager.lookup();
	    sManager.startMonitoring();
	} catch (RemoteException e) {
	    System.out.println("CSBaseLoginProxy: Erro ao acessar servidor: "+e);
	} catch (CSBaseException e)  {
	    System.out.println("CSBaseLoginProxy: Erro no lookup: "+e);
	}
    }

    /**
     * Método sobrescrito porque, por alguma razão o jython do gluuserver não consegue acessar o java.util.Locale. O mesmo código funciona em todos os outros jythons testados
     */
    public boolean authenticate(String login, String passwd) {
	return this.authenticate(login, passwd, Locale.US);
    }

    /**
     * Autentica um determinado usuário com login e senha
     * @param login login do usuário
     * @param passwd senha do usuário
     * @param loc Locale escolhido pelo usuário
     * @return true se usuário foi logado com sucesso, false caso contrário
     */
    public boolean authenticate(String login, String passwd, Locale loc) {
	boolean ret = false;
	try {    
	    ServerEntryPoint serverEP = sManager.getServer();
	    if (serverEP != null) {
		Session session = serverEP.login(login, passwd, loc);
		if (session != null) {
		    ret = true;
		}
	    } else {
		System.out.println("CSBaseLoginProxy: Erro ao acessar servidor em authenticate: servidor nao alcancavel");
	    }
	} catch (RemoteException e) {
	    System.out.println("CSBaseLoginProxy: Erro ao fazer login: "+e);
	} 
	return ret;
    }

    public static void main(String[] args) {
	CSBaseLoginProxy lProxy = new CSBaseLoginProxy("localhost", 5099, false);
	System.out.println(lProxy.authenticate("lmachado", "leo", Locale.US));
	System.out.println(lProxy.authenticate("admin", "123455", Locale.US));
    }
}

/**
 * Classe utilitária para termos um ServerManager utilizável: SingleServerManager é abstrato. Métodos lang e login tem implementações vazias porque para fins de mera autenticação não são necessários (o login é feito através de outro método)
 */
class BasicServerManager extends SingleServerManager {
    public BasicServerManager (final ServerURI serverURI, final Class<?> locator, boolean verbose) {
	this(serverURI, locator, verbose, 5);
    }

    public BasicServerManager(final ServerURI serverURI, final Class<?> locator, boolean verbose, int windowSize) {
	super(serverURI, locator, verbose, windowSize);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String lang(final String key) {
	return "";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected final Session login() throws CSBaseException {
	return null;
    }
}