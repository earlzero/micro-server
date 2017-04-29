package app.swagger.com.aol.micro.server;

import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertThat;

import java.util.concurrent.ExecutionException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import com.aol.micro.server.MicroserverApp;
import com.aol.micro.server.testing.RestAgent;

@Configuration
@ComponentScan(basePackages = { "app.swagger.com.aol.micro.server" })
public class SwaggerRunnerTest {


	RestAgent rest = new RestAgent();
	
	MicroserverApp server;
	@Before
	public void startServer(){
		
		server = new MicroserverApp( SwaggerRunnerTest.class, ()-> "swagger-app");
		server.start();

	}
	
	@After
	public void stopServer(){
		server.stop();
	}
	
	@Test
	public void runAppAndBasicTest() throws InterruptedException, ExecutionException{
		
		
		
		assertThat(rest.getJson("http://localhost:10080/api-docs/stats"),containsString("Make a ping call"));
	
	}
	
	
	
}
