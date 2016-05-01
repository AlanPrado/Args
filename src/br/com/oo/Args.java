package br.com.oo;

import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

public class Args {
	
	private Map<Character, Function<String, ?>> schemaMap;
	private Map<Character, String> argumentMap;
	
	public Args(String schema, String[] arguments) throws ArgsException{
		schemaMap = parseSchema(schema);
		argumentMap = parseArguments(arguments);
	}
	
	@FunctionalInterface
	private interface Function<T, R> {
	   R apply(T t) throws ArgsException;
	}

	private Map<Character, String> parseArguments(String[] arguments) {
		Map<Character, String> map = new HashMap<>();
		for (String argument : arguments) {
			StringTokenizer st = new StringTokenizer(argument, "-");
			while(st.hasMoreTokens()) {
				String arg = st.nextToken().trim();
				map.put(arg.charAt(0), arg.length() > 1 ? arg.substring(1) : "true");
			}
		}
		return map;
	}

	public Map<Character, Function<String, ?>> parseSchema(String schema) throws ArgsException {
		StringTokenizer st = new StringTokenizer(schema, ",");
		Map<Character, Function<String, ?>> map = new HashMap<>();
		while(st.hasMoreTokens()) {
			String argument = st.nextToken();
			char id = argument.charAt(0);
			if(!Character.isLetter(id)) {
	    		throw new ArgsException(String.format("Invalid argument format: id %s is not a letter", id));
			}
			Character format = argument.length() == 1 ? null : argument.charAt(1);
			if(format == null) {
				map.put(id, arg -> parseBool(arg));
			} else if(format == '#'){
				map.put(id, arg -> parseInt(arg));
			} else if(format == '*'){
				map.put(id, arg -> parseStr(arg));
			} else {
	    		throw new ArgsException(String.format("Invalid argument format: format %s not found", format));
			}
		}
		return map;
	}
	
    private static Boolean parseBool(String s) throws ArgsException { 
        return s == null ? false : true;
    }
    
    private static Integer parseInt(String s) throws ArgsException { 
    	try {
    		return Integer.parseInt(s);    		
    	} catch (ArrayIndexOutOfBoundsException aio) {
    		throw new ArgsException(String.format("Invalid argument format: array index out bound exception: %s ", s), aio);
    	} catch (NumberFormatException nfe) {
    		throw new ArgsException(String.format("Invalid argument format: number format exception: %s ", s), nfe);
    	}
    }
    
    private static String parseStr(String s) throws ArgsException { 
    	return s;
    }
    
    public Object getValue(char m) throws ArgsException { 
    	return getValue(m, Object.class);
    }
    
    @SuppressWarnings("unchecked")
	public <T> T getValue(char m, Class<T> t) throws ArgsException {
		String s = argumentMap.get(m);
		if(s == null) {
			throw new ArgsException(String.format("Unexpected argument: %s", m));
		}
		Object r = schemaMap.get(m).apply(s);
		if(t.isInstance(r)) {
			return (T) r;
		} 
		throw new ArgsException(String.format("Invalid argument type: class cast exception from %s to %s ", r.getClass(), t));
    }
    
	public static void main(String[] arguments) {
		try {
			Args args = new Args("l,p#,d*", new String[]{"-l", "-p3", "-dXYZ"});
			Boolean logging = args.getValue('l', Boolean.class);
			Integer port = args.getValue('p', Integer.class);
			String directory = args.getValue('d', String.class);
			executeApplication(logging, port, directory);
		} catch (ArgsException e) {
			Exception ex = e.getException();
			if(ex != null)	{
				ex.printStackTrace();
			} else {
				System.out.println(e.getLocalizedMessage());
			}
		}
	}

	private static void executeApplication(Boolean logging, Integer port, String directory) {
		System.out.println(String.format("logging %b, port: %d, directory: %s", logging, port, directory));
	}
}

class ArgsException extends Exception {
	
	private static final long serialVersionUID = 7269083538313694834L;
	private Exception exception;
	
	public ArgsException(String msg) {
		this(msg, null);
	}
	
	public ArgsException(String msg, Exception ex) {
		super(msg);
		this.exception = ex;
	}
	
	public Exception getException() {
		return exception;
	}
}