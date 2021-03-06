package com.fringefy.urbo;

import java.lang.reflect.Type;
import java.util.Date;

import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.client.OkClient;
import retrofit.converter.GsonConverter;
import android.util.Base64;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.squareup.okhttp.OkHttpClient;

abstract class OdieFactory {

	private static final String USERNAME = "lior";
	private static final String PASSWORD = "55555";

	public static Gson getGson() {
		return new GsonBuilder()
			.registerTypeAdapter(Date.class, new JsonDeserializer<Date>() {

				@Override
				public Date deserialize(JsonElement json, Type typeOfT,
				                        JsonDeserializationContext context)
						throws JsonParseException {

					return new Date(json.getAsJsonPrimitive().getAsLong());
				}

			}).registerTypeAdapter(Date.class, new JsonSerializer<Date>() {

                    @Override
                    public JsonElement serialize(Date d, Type typeOfT,
                                                 JsonSerializationContext context) {

                        return new JsonPrimitive(d.getTime());
                    }

            }).registerTypeAdapter(RecoEvent[].class, new JsonSerializer<RecoEvent[]>() {

                //TODO: [AC] Why is this required?
				@Override
				public JsonElement serialize(RecoEvent[] array, Type typeOfT,
				                             JsonSerializationContext context) {

					if (array == null) {
						return null;
					}
					JsonArray ja = new JsonArray();
					for (RecoEvent re: array) {
						ja.add(context.serialize(re, RecoEvent.class));
					}
					return ja;
				}

			}).registerTypeAdapter(Usig.class, new JsonSerializer<Usig>() {

                /* TODO: [AC] Move this logic to the Poi class.
                    This logic is way to specific to be hidden so far away from the target (Poi).
                    Use something of the sort of http://stackoverflow.com/a/17733569/2099542 to
                    annotate the Usig field as a "downstream only" field
                 */
				@Override
				public JsonElement serialize(Usig usig, Type typeOfT,
                                             JsonSerializationContext context) {
					return null;
				}

			}).create();
	}

	static Odie getInstance(String sEndpoint) {

		RestAdapter restAdapter = new RestAdapter.Builder()
				.setLogLevel(RestAdapter.LogLevel.FULL)
				.setEndpoint(sEndpoint)
				.setRequestInterceptor(new RequestInterceptor() {
					@Override public void intercept(RequestFacade request) {
						String sAuth = "Basic " + Base64.encodeToString(
								(USERNAME + ":" + PASSWORD).getBytes(), Base64.NO_WRAP);
						request.addHeader("Accept", "application/json");
						request.addHeader("Authorization", sAuth);
					}
				})
				.setConverter(new GsonConverter(getGson()))
				.setClient(new OkClient(new OkHttpClient()))
				.build();

		return restAdapter.create(Odie.class);
	}
}