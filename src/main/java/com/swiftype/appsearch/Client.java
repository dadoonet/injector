package com.swiftype.appsearch;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.http.HttpHeaders;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.URI;
import java.security.InvalidKeyException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Api client for Swiftype App Search.
 *
 * @see <a href="https://swiftype.com/documentation/app-search/">https://swiftype.com/documentation/app-search/</a>
 */
public class Client {
  // Remember to also update version in build.gradle!
  private final String VERSION = "0.3.0";

  private static final Logger logger = LogManager.getLogger(Client.class);

  private final String baseUrl;
  private final String username;
  private final String password;

  /**
   * @param baseUrl url of the service
   * @param username elastic username
   * @param password elastic password
   */
  public Client(String baseUrl, String username, String password) {
    this.baseUrl = baseUrl;
    this.username = username;
    this.password = password;
  }

  /**
   * Search for documents.
   *
   * @param engineName unique engine name
   * @param query search query string
   * @return search results
   * @throws ClientException if the api request fails
   */
  public Map<String, Object> search(String engineName, String query) throws ClientException {
    return search(engineName, query, Collections.emptyMap());
  }

  /**
   * Search for documents.
   *
   * @param engineName unique engine name
   * @param query search query string
   * @param options see the <a href="https://swiftype.com/documentation/app-search/">App Search API</a> for supported search options.
   * @return search results
   * @throws ClientException if the api request fails
   */
  public Map<String, Object> search(String engineName, String query, Map<String, Object> options) throws ClientException {
    Map<String, Object> reqBody = new HashMap<>(options);
    reqBody.put("query", query);

    return makeJsonRequest("GET", String.format("engines/%s/search", engineName), reqBody, JsonTypes.OBJECT);
  }

  /**
   * Lists the first 20 engines that the api key has access to.
   * @return engines list
   * @throws ClientException if the api request fails
   */
  public Map<String, Object> listEngines() throws ClientException {
    return listEngines(1, 20);
  }

  /**
   * Lists engines that the api key has access to.
   * @param current current page number
   * @param size number of engines per page
   * @return engines list
   * @throws ClientException if the api request fails
   */
  public Map<String, Object> listEngines(Integer current, Integer size) throws ClientException {
    Map<String, Object> pageOptions = new HashMap<>();
    pageOptions.put("current", current);
    pageOptions.put("size", size);

    Map<String, Object> reqBody = new HashMap<>();
    reqBody.put("page", pageOptions);

    return makeJsonRequest("GET", "engines", reqBody, JsonTypes.OBJECT);
  }

  /**
   * Retrieves an engine by name.
   * @param engineName unique engine name
   * @return an engine
   * @throws ClientException if the api request fails
   */
  public Map<String, Object> getEngine(String engineName) throws ClientException {
    return makeJsonRequest("GET", String.format("engines/%s", engineName), null, JsonTypes.OBJECT);
  }

  /**
   * Creates an engine with the specified name.
   * @param engineName unique engine name
   * @return engine name
   * @throws ClientException if the api request fails
   */
  public Map<String, Object> createEngine(String engineName) throws ClientException {
    Map<String, String> reqBody = new HashMap<>();
    reqBody.put("name", engineName);

    return makeJsonRequest("POST", "engines", reqBody, JsonTypes.OBJECT);
  }

  /**
   * Destroys an engine by name.
   * @param engineName unique engine name
   * @return engine destroy status
   * @throws ClientException if the api request fails
   */
  public Map<String, Boolean> destroyEngine(String engineName) throws ClientException {
    Map<String, Object> response = makeJsonRequest("DELETE", String.format("engines/%s", engineName), null, JsonTypes.OBJECT);
    Map<String, Boolean> convertedResponse = new HashMap<>();
    for (Map.Entry<String, Object> e : response.entrySet()) {
      convertedResponse.put(e.getKey(), (Boolean) e.getValue());
    }
    return convertedResponse;
  }

  /**
   * Index a single document.
   * @param engineName unique engine name
   * @param document A single document to index
   * @return a document creation status
   * @throws ClientException if the api request fails or if there were errors in processing the document
   */
  public Map<String, Object> indexDocument(String engineName, Map<String, Object> document) throws ClientException {
    List<Map<String, Object>> documents = Collections.singletonList(document);
    List<Map<String, Object>> response = indexDocuments(engineName, documents);

    Map<String, Object> documentIndexingStatus = response.get(0);

    @SuppressWarnings("unchecked")
    List<String> errors = (List<String>) documentIndexingStatus.remove("errors");
    if (errors.size() > 0) {
      String errorMessage = String.format("Invalid document: %s", String.join("; ", errors));
      throw new InvalidDocumentException(errorMessage);
    }

    return documentIndexingStatus;
  }

  /**
   * Index a single document.
   * @param engineName unique engine name
   * @param document A single document to index
   * @return a document creation status
   * @throws ClientException if the api request fails or if there were errors in processing the document
   */
  public Map<String, Object> indexJsonDocument(String engineName, String document) throws ClientException {
    List<String> documents = Collections.singletonList(document);
    List<Map<String, Object>>  response = indexJsonDocuments(engineName, documents);

    Map<String, Object> documentIndexingStatus = response.get(0);

    @SuppressWarnings("unchecked")
    List<String> errors = (List<String>) documentIndexingStatus.remove("errors");
    if (errors.size() > 0) {
      String errorMessage = String.format("Invalid document: %s", String.join("; ", errors));
      throw new InvalidDocumentException(errorMessage);
    }

    return documentIndexingStatus;
  }

  /**
   * Index a batch of documents.
   *
   * @param engineName unique engine name
   * @param documents collection of document objects to index
   * @return list of document creation statuses
   * @throws ClientException if the api request fails
   */
  public List<Map<String, Object>> indexDocuments(String engineName, List<Map<String, Object>> documents) throws ClientException {
    return makeJsonRequest("POST", String.format("engines/%s/documents", engineName), documents, JsonTypes.ARRAY_OF_OBJECTS);
  }

  /**
   * Index a batch of documents.
   *
   * @param engineName unique engine name
   * @param documents collection of document objects to index
   * @return list of document creation statuses
   * @throws ClientException if the api request fails
   */
  public List<Map<String, Object>> indexJsonDocuments(String engineName, List<String> documents) throws ClientException {
    return makeJsonRawRequest("POST", String.format("engines/%s/documents", engineName), documents, JsonTypes.ARRAY_OF_OBJECTS);
  }

  /**
   * Retrieve a batch of documents.
   *
   * @param engineName unique engine name
   * @param ids batch of document ids to retrieve
   * @return list of document details
   * @throws ClientException if the api request fails
   */
  public List<Map<String, Object>> getDocuments(String engineName, List<String> ids) throws ClientException {
    return makeJsonRequest("GET", String.format("engines/%s/documents", engineName), ids, JsonTypes.ARRAY_OF_OBJECTS);
  }

  /**
   * Destroy a batch of documents.
   *
   * @param engineName unique engine name
   * @param ids batch of document ids to destroy
   * @return  list of document deletion statuses
   * @throws ClientException if the api request fails
   */
  public List<Map<String, Object>> destroyDocuments(String engineName, List<String> ids) throws ClientException {
    return makeJsonRequest("DELETE", String.format("engines/%s/documents", engineName), ids, JsonTypes.ARRAY_OF_OBJECTS);
  }

  /**
   * Creates a jwt search key that can be used for authentication to enforce a set of required search options.
   *
   * @param apiKeyName the unique name for the API Key
   * @param options see the <a href="https://swiftype.com/documentation/app-search/">App Search API</a> for supported search options
   * @return jwt search key
   * @throws InvalidKeyException if the api key is invalid
   */
  public static String createSignedSearchKey(String apiKey, String apiKeyName, Map<String, Object> options) throws InvalidKeyException {
    Map<String, Object> payload = new HashMap<>(options);
    payload.put("api_key_name", apiKeyName);
    return Jwt.sign(apiKey, payload);
  }

  private <T> T makeJsonRequest(String httpMethod, String path, Object body, TypeToken<T> resultType) throws ClientException {
    String reqBody = null;
    if (body != null) {
      reqBody = new Gson().toJson(body);
    }

    return makeJsonRequestWithJsonBody(httpMethod, path, reqBody, resultType);
  }

  private <T> T makeJsonRawRequest(String httpMethod, String path, List<String> requests, TypeToken<T> resultType) throws ClientException {
    StringBuilder sb = new StringBuilder("[");
    boolean first = true;
    for (String request : requests) {
        if (!first) {
            sb.append(",");
        } else {
            first = false;
        }
      sb.append(request);
    }

    sb.append("]");
    String payload = sb.toString();
    logger.trace(payload);
    return makeJsonRequestWithJsonBody(httpMethod, path, payload, resultType);
  }

  private <T> T makeJsonRequestWithJsonBody(String httpMethod, String path, String body, TypeToken<T> resultType) throws ClientException {
    try {
      CredentialsProvider provider = new BasicCredentialsProvider();
      UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(username, password);
      provider.setCredentials(AuthScope.ANY, credentials);

      try (CloseableHttpClient httpClient = HttpClientBuilder.create().setDefaultCredentialsProvider(provider).build()) {
        HttpDynamicRequestWithBody request = new HttpDynamicRequestWithBody(httpMethod, baseUrl + path);
        request.setHeader("X-Swiftype-Client", "swiftype-app-search-java");
        request.setHeader("X-Swiftype-Client-Version", VERSION);
        request.setHeader(HttpHeaders.CONTENT_TYPE, "application/json");

        if (body != null) {
          request.setEntity(new StringEntity(body));
        }

        try (CloseableHttpResponse response = httpClient.execute(request)) {
          int statusCode = response.getStatusLine().getStatusCode();
          if (statusCode < 200 || statusCode > 299) {
            throw new ClientException(statusCode, String.format("Error: %d", statusCode));
          }
          String respBody = EntityUtils.toString(response.getEntity());
          return new Gson().fromJson(respBody, resultType.getType());
        }
      }
    } catch (IOException e) {
      throw new ClientException("Error making http request", e);
    }
  }

  private static class HttpDynamicRequestWithBody extends HttpEntityEnclosingRequestBase {
    private final String method;

    public HttpDynamicRequestWithBody(String method, String uri) {
      this.method = method;
      setURI(URI.create(uri));
    }

    @Override
    public String getMethod() {
      return this.method;
    }
  }
}
