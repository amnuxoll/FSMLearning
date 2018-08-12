
import org.junit.jupiter.api.Assertions;

import java.util.ArrayList;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;


public class DecarbonatedAgentTest {

	@Test
	public void testGetTryCount() throws Exception{
		DecarbonatedAgent testAgent = new DecarbonatedAgent(); //our agent
		
		String[] suffix = {"ac", "a", "ab", "cb", "abc"}; //sequences to try
		
		//a bunch of staged episodic memories
		testAgent.episodicMemory.add(new Episode('a', 0));
		testAgent.episodicMemory.add(new Episode('c', 0));
		testAgent.episodicMemory.add(new Episode('b', 0));
		testAgent.episodicMemory.add(new Episode('a', 0));
		testAgent.episodicMemory.add(new Episode('a', 0));
		testAgent.episodicMemory.add(new Episode('b', 0));
		testAgent.episodicMemory.add(new Episode('a', 0));
		testAgent.episodicMemory.add(new Episode('a', 0));
		testAgent.episodicMemory.add(new Episode('c', 0));
		testAgent.episodicMemory.add(new Episode('a', 0));
		testAgent.episodicMemory.add(new Episode('c', 0));
		testAgent.episodicMemory.add(new Episode('c', 0));
		testAgent.episodicMemory.add(new Episode('c', 0));
		testAgent.episodicMemory.add(new Episode('b', 1));
		testAgent.episodicMemory.add(new Episode('a', 0));
		testAgent.episodicMemory.add(new Episode('a', 0));
		testAgent.episodicMemory.add(new Episode('a', 0));
		testAgent.episodicMemory.add(new Episode('b', 0));
		testAgent.episodicMemory.add(new Episode('a', 0));
		
		int[] testAC = testAgent.getTryCount(suffix[0]); //test suffix results, "ac"
		
		assertEquals(testAC[0], 3); //numtries
		assertEquals(testAC[1], 3); //numfails
		
		testAC = testAgent.getTryCount(suffix[1]); //"a"
		
		assertEquals(testAC[0], 10); //numtries
		assertEquals(testAC[1], 10); //numfails
		
		testAC = testAgent.getTryCount(suffix[2]); //"ab"
		
		assertEquals(testAC[0], 2); //numtries
		assertEquals(testAC[1], 2); //numfails
		
		testAC = testAgent.getTryCount(suffix[3]); //"cb"
		
		assertEquals(testAC[0], 2); //numtries
		assertEquals(testAC[1], 1); //numfails
		
		testAC = testAgent.getTryCount(suffix[4]); //"abc"
		
		assertEquals(testAC[0], 0); //numtries
		assertEquals(testAC[1], 0); //numfails
	}
	
	@Test
	public void testExpandNode() throws Exception{
		DecarbonatedAgent testAgent = new DecarbonatedAgent(); //our test agent
		testAgent.alphabet = new char[2];
		testAgent.alphabet[0] = 'a';
		testAgent.alphabet[1] = 'b';
		testAgent.expandNode();
		
	}

}
