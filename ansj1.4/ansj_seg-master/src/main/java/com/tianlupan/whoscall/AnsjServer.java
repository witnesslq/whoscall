package com.tianlupan.whoscall;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import org.ansj.splitWord.analysis.NlpAnalysis;
import org.ansj.splitWord.analysis.ToAnalysis;
import org.ansj.util.MyStaticValue;
import org.nlpcn.commons.lang.util.IOUtil;
import org.nlpcn.commons.lang.util.StringUtil;

import com.tianlupan.whoscall.AnsjServlet.OnCallback;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.spi.HttpServerProvider;

/**
 * http 服务类 测试网址: http://localhost:8888/page/test.html
 * 
 * @author ansj
 * 
 */
@SuppressWarnings("all")
public class AnsjServer {

	private final static String WARM_UP_WORD = "你好";

	private final static String CLOSE_COMMAND = "关闭服务器";
	
	private final static String ACTION_PATH="api.action";
	private final static String WWW_ROOT_DIR="page";
	private final static String DEFAULT_PAGE="index.html";
	
	private final static int DEFAULT_PORT=8080;

	private static final String FILE_ENCODING = System
			.getProperty("file.encoding");

	private static HttpServer httpserver;

	public void startServer(int serverPort) throws Exception {
		MyStaticValue.LIBRARYLOG.info("starting ansj http server");
		HttpServerProvider provider = HttpServerProvider.provider();
		httpserver = provider.createHttpServer(
				new InetSocketAddress(serverPort), 100);// 监听端口6666,能同时接
														// 受100个请求
		httpserver.createContext("/", new AnsjHttpHandler());
		httpserver.setExecutor(null);
		httpserver.start();
		System.out.println("server started");

	}

	public static void stopServer() {
		httpserver.stop(0);
		System.out.println("server stopped");
		System.exit(0);
	}

	private static class AnsjHttpHandler implements HttpHandler {
		public void handle(final HttpExchange httpExchange) {

			boolean shouldExist = false;

			try {
				String path = httpExchange.getRequestURI().getPath();
				
				if(TextUtils.isEmpty(path) || path.equals("/")  ) path="/"+ DEFAULT_PAGE;
				path=WWW_ROOT_DIR+path;
				System.out.println("path="+path);
				
				if (!path.contains(ACTION_PATH)) {
		/*			httpExchange.getResponseHeaders().set("Content-Type",
							"text/html; charset=utf-8");
					writeToClient(httpExchange, readFileToString(path));
					return;*/
					writeToClientFile(httpExchange, path);
					return;
				}

				String responseMsg = "<html>..<a href='/test.html'>查看演示!</a><script>location.href='/test.html'</script></html>"; // 响应信息
				// String responseMsg="请求格式错误";
				httpExchange.getResponseHeaders().set("Content-Type",
						"text/html; charset=utf-8");

				Map<String, String> paramers = parseParamers(httpExchange);
				String input = paramers.get("phone");

				int method = NTUtil.defaultMethod;

				if (paramers.containsKey("method")) {
					String methodString = paramers.get("method");
					// ToAnalysis
					if (methodString.equals("2"))
						method = 2;
					// NlpAnalysis
					else if (methodString.equals("1"))
						method = 1;
				}

				if (StringUtil.isNotBlank(input)) {
					if (input.equals(CLOSE_COMMAND)) {
						shouldExist = true;
						responseMsg = "服务器已关闭，请勿重新提交请求";
						writeToClient(httpExchange, responseMsg);
						return;
					} else {
						httpExchange.getResponseHeaders().set("Content-Type",
								"application/Json; charset=utf-8");
						httpExchange.getResponseHeaders().set(
								"Transfer-Encoding", "chunked");
						
						httpExchange.sendResponseHeaders(200, 0L);

						AnsjServlet.processRequest(input, method,
								new OnCallback() {

									public void onResult(String result) {
										writeToClientChunk(httpExchange, result);
										// 代表结束输出
										// writeToClientChunk(httpExchange, "");
									}

									public void onFinish() {
										writeToClientChunk(httpExchange, "");
									}

									public void onError(int errorCode,
											String errorMsg) {
										writeToClientChunk(httpExchange,
												"{\"found\":false, \"error_code\":"
														+ errorCode
														+ ", \"error\":\""
														+ errorMsg + "\"}");
										writeToClientChunk(httpExchange, "");
									}
								});

					}
				}

			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
				try {
					writeToClient(httpExchange, e.getMessage());
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					System.out.println("eee");
				}

			} finally {
				httpExchange.close();
			}

			if (shouldExist) {
				stopServer();
			}

		}

		private String readFileToString(String path) {
			InputStream resourceAsStream = null;
			try {
				resourceAsStream = new FileInputStream(path);
				if(resourceAsStream==null) return  String.valueOf("发生了错误 :404 文件找不到鸟！");
				return IOUtil.getContent(resourceAsStream, IOUtil.UTF8);
			} catch (Exception e) {
				return String.valueOf("发生了错误 :404 文件找不到鸟！");
			} finally {
				if (resourceAsStream != null) {
					try {
						resourceAsStream.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}

		private void writeToClientChunk(HttpExchange httpExchange,
				String responseMsg) {

			try {
				OutputStream out = httpExchange.getResponseBody();
				byte[] bytes = responseMsg.getBytes();
				if (bytes.length > 0) {
					 // 获得输出流
					out.write(bytes);
					out.flush();
				} else
					out.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		private void writeToClient(HttpExchange httpExchange, String responseMsg)
				throws IOException {
			/*
			 * byte[] bytes = responseMsg.getBytes();
			 * 
			 * 
			 * httpExchange.sendResponseHeaders(200, bytes.length); //
			 * 设置响应头属性及响应信息的长度
			 * 
			 * 
			 * OutputStream out = httpExchange.getResponseBody(); // 获得输出流
			 * out.write(bytes); out.flush(); out.close();
			 */

			httpExchange.getResponseHeaders().set("Transfer-Encoding",
					"chunked");
			httpExchange.sendResponseHeaders(200, 0L);

			writeToClientChunk(httpExchange, responseMsg);
			writeToClientChunk(httpExchange, "");
		}
		
		private void writeToClientFile(HttpExchange httpExchange, String path)
				throws IOException {
			
			byte[] defaultString=String.valueOf("发生了错误 :404 文件找不到鸟！").getBytes(Charset.forName("utf-8"));
			InputStream resourceAsStream = null;
			
			boolean isText=true;
			
			String contentType="text/html";
			
			if(path.toLowerCase().endsWith("gif") || path.toLowerCase().endsWith("jpg") || path.endsWith("png") )
			{
				contentType="image/jpg";
			}
			else if(path.toLowerCase().endsWith(".js"))
			{
				contentType="application/Json";
			}
			
			byte[] bytes=null;
			try {
//				resourceAsStream = this.getClass().getResourceAsStream(path);
				resourceAsStream=new FileInputStream(path);
				FileInputStream stream;
				
				if(resourceAsStream!=null)
					{
						int size= resourceAsStream.available();
						System.out.println("file="+path+",available="+size);
						
						int readCount=0;
						int readTotalCount=0;
						bytes=new byte[size];
						while((readCount=resourceAsStream.read(bytes, readTotalCount, size-readTotalCount))>0)
						{
							readTotalCount+=readCount;
						}
					}
				else {
					bytes=defaultString;
					contentType="text/html";
				}
				
				
				httpExchange.sendResponseHeaders(200, bytes.length);
				httpExchange.getResponseHeaders().set("Content-Type",
						contentType+"; charset=utf-8");
				
				//设置响应头属性及响应信息的长度
				  OutputStream out = httpExchange.getResponseBody(); // 获得输出流
				  out.write(bytes);
				  out.flush();
				  out.close();
				
			} catch (Exception e) {
				//
			} finally {
				if (resourceAsStream != null) {
					try {
						resourceAsStream.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
			


		private Map<String, String> parseParamers(HttpExchange httpExchange)
				throws UnsupportedEncodingException, IOException {
			// TODO Auto-generated method stub
			BufferedReader reader = null;
			try {
				Map<String, String> parameters = new HashMap<String, String>();
				URI requestedUri = httpExchange.getRequestURI();
				String query = requestedUri.getRawQuery();
				// get 请求解析
				parseQuery(query, parameters);
				// post 请求解析
				reader = IOUtil.getReader(httpExchange.getRequestBody(),
						FILE_ENCODING);
				query = IOUtil.getContent(reader).trim();
				parseQuery(query, parameters);
				httpExchange.setAttribute("parameters", parameters);
				return parameters;
			} finally {
				if (reader != null) {
					reader.close();
				}
			}
		}

		/**
		 * 从get请求中解析参数
		 * 
		 * @param query
		 * @param parameters
		 */
		private void parseQuery(String query, Map<String, String> parameters) {
			// TODO Auto-generated method stub
			if (StringUtil.isBlank(query)) {
				return;
			}
			String split[] = query.split("\\?");
			query = split[split.length - 1];
			split = query.split("&");
			String[] param = null;
			String key = null;
			String value = null;
			for (String kv : split) {
				try {
					param = kv.split("=");
					if (param.length == 2) {
						key = URLDecoder.decode(param[0], FILE_ENCODING);
						value = URLDecoder.decode(param[1], FILE_ENCODING);
						parameters.put(key, value);
					}
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

		}
	}

	public static void main(String[] args) throws Exception {
		if (args == null || args.length < 1) {
			System.err
					.println("Usage: AnsjServer <serverPort> so by default "+DEFAULT_PORT);
			args = new String[] { DEFAULT_PORT+"" };
		}

		System.out.println("服务器准备启动");
		/* set up server */
		int serverPort = Integer.valueOf(args[0]);
		new AnsjServer().startServer(serverPort);
		// System.out.println("This will take lots of memory...");
		NTUtil.initNTNZ();
		System.out.println("初始化NT字典成功");
		/* warm up ansj engine */
		/* FIXME: dirty hack here... */
		// List<Term> terms=
		if (NTUtil.defaultMethod == 1) {
			NlpAnalysis.parse(WARM_UP_WORD);
		} else {
			ToAnalysis.parse(WARM_UP_WORD);
		}

		// new NatureRecognition(terms).recognition(); //词性标注

		// System.out.println(terms);
		System.out.println("服务器启动完成，网址：");
		System.out
				.println("http://localhost" + (serverPort==80 ? "" : ":"+ serverPort) );
		System.out.println("服务启动完成");
	}
}
