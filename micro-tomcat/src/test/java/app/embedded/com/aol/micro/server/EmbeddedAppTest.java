package app.embedded.com.aol.micro.server;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import javax.ws.rs.NotFoundException;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

import com.aol.micro.server.MicroserverApp;
import com.aol.micro.server.module.EmbeddedModule;
import com.aol.micro.server.testing.RestAgent;
@Ignore //micro-monolith doesn't work with Tomcat at the moment, because tomcat MBean registrations clash
public class EmbeddedAppTest {
	
	RestAgent rest = new RestAgent();
	
	MicroserverApp server;
	
	@Before
	public void startServer(){
		server = new MicroserverApp(EmbeddedAppLocalMain.class, 
				EmbeddedModule.tagInterfaceModule(Arrays.asList(TestAppRestResource.class),"test-app"),
				EmbeddedModule.tagInterfaceModule(Arrays.asList(AltAppRestResource.class),"alternative-app"));
		server.start();
	}
	
	@After
	public void stopServer(){
		server.stop();
	}
	
	@Test
	public void confirmExpectedUrlsPresentTest() throws InterruptedException, ExecutionException{
		
		assertThat(rest.get("http://localhost:10080/test-app/test-status/ping"),is("test!"));
		
		
		assertThat((List<String>)rest.post("http://localhost:10081/alternative-app/alt-status/ping",new ImmutableEntity("value",Arrays.asList("hello","world")),List.class),
				hasItem("hello"));
	
	}
	
	
	@Test
	public void nonBlockingRestClientTest(){
		assertThat(rest.get("http://localhost:10080/test-app/test-status/rest-calls"),is("-*test!-*test!"));
	}
	
	<T> CompletableFuture<T> toCompletableFuture(
			final ListenableFuture<T> listenableFuture
		) {
	        //create an instance of CompletableFuture
	        CompletableFuture<T> completable = new CompletableFuture<T>() {
	            @Override
	            public boolean cancel(boolean mayInterruptIfRunning) {
	                // propagate cancel to the listenable future
	                boolean result = listenableFuture.cancel(mayInterruptIfRunning);
	                super.cancel(mayInterruptIfRunning);
	                return result;
	            }
	        };

	        // add callback
	        listenableFuture.addCallback(new ListenableFutureCallback<T>() {
	            @Override
	            public void onSuccess(T result) {
	                completable.complete(result);
	            }

	            @Override
	            public void onFailure(Throwable t) {
	                completable.completeExceptionally(t);
	            }
	        });
	        return completable;
	    }
	
	@Test(expected=NotFoundException.class)
	public void confirmAltAppCantUseTestAppResources(){
		
		assertThat(rest.get("http://localhost:10080/alternative-app/test-status/ping"),is("test!"));
	
	}
	@Test(expected=NotFoundException.class)
	public void confirmTestAppCantUseAltAppResources(){
		
		assertThat((List<String>)rest.post("http://localhost:10081/test-app/alt-status/ping",new ImmutableEntity("value",Arrays.asList("hello","world")),List.class),
				hasItem("hello"));
	
	}
}
