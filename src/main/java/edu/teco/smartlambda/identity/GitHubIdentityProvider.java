package edu.teco.smartlambda.identity;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import edu.teco.smartlambda.Application;
import edu.teco.smartlambda.authentication.entities.User;
import org.apache.commons.lang3.tuple.Pair;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import static org.torpedoquery.jpa.Torpedo.from;
import static org.torpedoquery.jpa.Torpedo.select;
import static org.torpedoquery.jpa.Torpedo.where;

/**
 * Created on 28.02.17.
 */
public class GitHubIdentityProvider implements IdentityProvider{
	private static final String NAME = "github";
	private final String GITHUB_URL = "https://api.github.com/user";
	
	@Override
	public Pair<User, String> register(final Map<String, String> parameters) throws IdentityException {
		final String accessToken = parameters.get("accessToken");
		if (accessToken == null) {
			throw new IdentitySyntaxException();
		}
		
		final GitHubCredential query = from(GitHubCredential.class);
		where(query.getAccessToken()).eq(accessToken);
		if (select(query).get(Application.getInstance().getSessionFactory().getCurrentSession()).isPresent()) throw new
				GitHubCredentialDuplicateException("Account already exists");
		
		final String name = this.gitHubRequest(accessToken);
		
		final Pair<User, String> returnvalue = User.createUser(name);
		
		final GitHubCredential credential = new GitHubCredential(accessToken, returnvalue.getLeft());
		Application.getInstance().getSessionFactory().getCurrentSession().save(credential);
				
		return returnvalue;
	}
	
	@Override
	public String getName() {
		return NAME;
	}
	
	private String gitHubRequest(final String accessToken) {
		final String name;
		//TODO ask GitHub for the name, throw Exception otherwise on error throw InvalidCredentialsException
		try {
			final URL               url        = new URL(this.GITHUB_URL);
			final HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("GET");
			connection.setRequestProperty("Accept", "application/json");
			connection.setRequestProperty("Authorization", "token " + accessToken);
			if (connection.getResponseCode() != 200) {
				final BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
				String output = "";
				String read;
				while ((read = reader.readLine()) != null) {
					output += read;
				}
				throw new InvalidCredentialsException(connection.getHeaderField(0) + ": " + output);
			}
			
			final JsonObject json = new Gson().fromJson(new InputStreamReader(connection.getInputStream()), JsonObject.class);
			name = json.get("login").getAsString();
			
			connection.disconnect();
		} catch (final MalformedURLException e) {
			throw new IdentitySyntaxException(e);
		} catch (final IOException e) {
			throw new IdentityException(e);
		}
		return name;
	}
}
