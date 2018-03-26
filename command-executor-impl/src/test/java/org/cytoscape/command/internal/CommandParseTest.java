package org.cytoscape.command.internal;

import static com.google.common.collect.ImmutableMap.of;
import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class CommandParseTest {

	@Parameters
	public static Collection<Object[]> data() {
		return Arrays.asList(new Object[][] { 
			
			// basic stuff
			{ "my command arg=hello", "my command", of("arg", "hello") },
			{ "my command name arg=hello", "my command name", of("arg", "hello") },
			{ "   my    command    name    arg=hello    ", "my command name", of("arg", "hello") },
			{ "my command arg=1", "my command", of("arg", "1") },
			{ "my command a=1 b=2", "my command", of("a", "1", "b", "2") },
			{ "my command a  =  1 b  =  2", "my command", of("a", "1", "b", "2") },
			
			// strings and escaping
			{ "command a=\"hello\"", "command", of("a", "hello") },
			{ "command a='hello'", "command", of("a", "hello") },
			{ "command a='he\"ll\"o'", "command", of("a", "he\"ll\"o") },
			{ "command a=\"he'll'o\"", "command", of("a", "he'll'o") },
			{ "command a=\"he'll'o\"", "command", of("a", "he'll'o") },
			
			// json
			{ "command json='{ \"key\" : \"value\" }'", "command", of("json", "{ \"key\" : \"value\" }") },
			{ "command json=\"{ \\\"key\\\" : \\\"value\\\" }\"", "command", of("json", "{ \"key\" : \"value\" }") },
			
		});
	}

	@Parameter(0) public String command;
	@Parameter(1) public String expectedRes;
	@Parameter(2) public Map<String,Object> expectedArgs;

	@Test
	public void testCommandParsing() {
		System.out.println(command);
		System.out.println(expectedRes);
		System.out.println(expectedArgs);
		
		Map<String,Object> arguments = new HashMap<>();
		String res = CommandExecutorImpl.parseInput(command, arguments);
		assertEquals(expectedRes, res);
		assertEquals(expectedArgs.size(), arguments.size());
		for(Map.Entry<String, Object> expectedArg : expectedArgs.entrySet()) {
			assertEquals(expectedArg.getValue(), arguments.get(expectedArg.getKey()));
		}
	}
		
}
