/*
 * Copyright © 2016 Yan Zhenjie.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.runvision.webcore.handler;

import com.google.gson.Gson;
import com.runvision.bean.WebDataResultJson;
import com.runvision.db.Admin;
import com.runvision.g68a_sn.MyApplication;
import com.yanzhenjie.andserver.RequestHandler;
import com.yanzhenjie.andserver.RequestMethod;
import com.yanzhenjie.andserver.annotation.RequestMapping;
import com.yanzhenjie.andserver.util.HttpRequestParser;

import org.apache.httpcore.HttpException;
import org.apache.httpcore.HttpRequest;
import org.apache.httpcore.HttpResponse;
import org.apache.httpcore.entity.StringEntity;
import org.apache.httpcore.protocol.HttpContext;

import java.io.IOException;
import java.util.Map;

/**
 * <p>Login Handler.</p>
 * Created by Yan Zhenjie on 2016/6/13.
 */
public class LoginHandler implements RequestHandler {


    @RequestMapping(method = {RequestMethod.POST})
    @Override
    public void handle(HttpRequest request, HttpResponse response, HttpContext context) throws HttpException, IOException {
        Map<String, String> params = HttpRequestParser.parseParams(request);
        String userName = params.get("username");
        String password = params.get("password");


        Admin admin = new Admin(userName, password);
        Admin admin1 = MyApplication.faceProvider.queryAdmin(admin);
        if (admin1 != null) {
            WebDataResultJson<Admin> dataResultJson = new WebDataResultJson<>(200, admin1);
            Gson gson = new Gson();
            String json = gson.toJson(dataResultJson);
            System.out.println("json="+json);
            StringEntity entity = new StringEntity(json, "UTF-8");
            response.setEntity(entity);
            response.setStatusCode(200);
        }else{
            WebDataResultJson<Admin> dataResultJson = new WebDataResultJson<>(404, null);
            Gson gson = new Gson();
            String json = gson.toJson(dataResultJson);
            System.out.println("json="+json);
            StringEntity entity = new StringEntity(json, "UTF-8");
            response.setEntity(entity);
            response.setStatusCode(400);
        }
    }
}
