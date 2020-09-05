package com.swiftype.appsearch;

import com.google.gson.reflect.TypeToken;

import java.util.List;
import java.util.Map;

class JsonTypes {
  static final TypeToken<Map<String, Object>> OBJECT = new TypeToken<Map<String, Object>>() { };
  static final TypeToken<List<Map<String, Object>>> ARRAY_OF_OBJECTS = new TypeToken<List<Map<String, Object>>>() { };
}

