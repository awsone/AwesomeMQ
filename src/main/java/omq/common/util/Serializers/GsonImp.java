package omq.common.util.Serializers;

import java.util.List;

import omq.common.message.Request;
import omq.common.message.Response;
import omq.exception.OmqException;
import omq.exception.SerializerException;
import omq.server.RemoteObject;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Json serialize implementation. It uses the Gson libraries.
 * 
 * @author Sergi Toda <sergi.toda@estudiants.urv.cat>
 * 
 */
public class GsonImp implements ISerializer {
	private final Gson gson = new Gson();

	@Override
	public byte[] serialize(Object obj) throws SerializerException {
		String json = gson.toJson(obj);
		return json.getBytes();
	}

	@Override
	public Request deserializeRequest(byte[] bytes, RemoteObject obj) throws SerializerException {
		String json = new String(bytes);

		JsonParser parser = new JsonParser();
		JsonObject jsonObj = parser.parse(json).getAsJsonObject();

		String id = jsonObj.get("id").getAsString();
		String method = jsonObj.get("method").getAsString();
		boolean async = jsonObj.get("async").getAsBoolean();

		List<Class<?>> types = obj.getParams(method);

		try {
			JsonArray jsonArgs = (JsonArray) jsonObj.get("params");

			// TODO: if (jsonArgs.size() == types.size())
			int length = jsonArgs.size();
			Object[] arguments = new Object[length];

			int i = 0;
			for (JsonElement element : jsonArgs) {
				arguments[i] = gson.fromJson(element, types.get(i));
				i++;
			}
			return new Request(id, method, async, arguments);
		} catch (NullPointerException e) {
			return new Request(id, method, async, null);
		}
	}

	@Override
	public Response deserializeResponse(byte[] bytes, Class<?> type) throws SerializerException {
		String json = new String(bytes);

		JsonParser parser = new JsonParser();
		JsonObject jsonObj = parser.parse(json).getAsJsonObject();

		String id = jsonObj.get("id").getAsString();
		String idOmq = jsonObj.get("idOmq").getAsString();

		JsonElement jsonElement = jsonObj.get("result");
		Object result = gson.fromJson(jsonElement, type);

		JsonElement jsonError = jsonObj.get("error");
		OmqException error = gson.fromJson(jsonError, OmqException.class);

		return new Response(id, idOmq, result, error);
	}

}
