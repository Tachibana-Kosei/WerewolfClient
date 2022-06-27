import java.io.*;
import java.net.*;
import java.util.*;

/*複数の処理を並列実行できる「スレッド」という仕組みを使用しています。
* SendThread内のrun()で第9回のwhile文と同じことをやってます。
* ReadThread内のrun()で第8回のwhile文と同じことをやってます。
* よく見比べてみると、「あ、同じ処理書いてるな」って分かると思います。ごめんだけど頑張って見比べてください。
* スレッドを使うと、複数の処理を同時に実行させられます。
* 2つのrun()が同時に実行されるので、送信と受信が同時に行えます。
*
* 実行する時は、「サーバーのIPアドレス ポート番号」を引数に渡してください。第9回と同じやつ。ポート番号は6000で固定です。
* 例： ...\NetworkProgramming\WerewolfClient\src> java TextChatClient 192.168.0.23 6000 */

public class TextChatClient {
	//サーバーと接続するソケット
	static Socket wrSocket = null;
	//サーバーへデータを送信するためのOutputStream
	static OutputStream outStr = null;
	//サーバーからのデータを受け取るためのInputStream
	static InputStream inStr = null;

	/*main()は、データ送信用のSendThreadと受信用のReadThreadを起動して終わり
	* SendThreadとReadThreadのインスタンスが1個ずつ作成されて同時に動作します。
	* SendThreadとReadThreadのrun()が、それぞれ同時に処理をし続けてくれると思ってください。*/
	public static void main(String[] args) {
		//サーバーへの接続処理。
		int port = 0;
		try {
			port = Integer.parseInt(args[1]);
			wrSocket = new Socket(args[0], port);
			inStr = wrSocket.getInputStream();
			outStr = wrSocket.getOutputStream();
		} catch (Exception e) {
			System.out.println("Connect Error");
			System.exit(1);
		}
		/*SendThreadのインスタンス作成とstart()実行。start()を実行すると、その後自動でrun()が実行される仕様。*/
		SendThread sendThread=new SendThread();
		sendThread.start();
		/*ReadThreadのインスタンス作成とstart()実行。上に同じ。*/
		ReadThread readThread =new ReadThread();
		readThread.start();
	}

	/*メッセージを送信するためのスレッドクラス。
	* run()の中身が、ReadThreadのrun()と並列にずっと実行されます。*/
	public static class SendThread extends Thread {
		private int n = 0;
		private final byte[] buff = new byte[1024];

		@Override
		public void run() {
			/*ここのwhile文は第9回のものと結構同じです。*/
			while (true) {
				//キーボードからの入力受付
				try {
					n = System.in.read(buff);
				} catch (IOException e) {
					System.out.println("Error by read()");
					System.exit(1);
				}
				//サーバーにデータ送信
				try {
					outStr.write(buff, 0, n);
				} catch (IOException e) {
					System.out.println("Error by Stream write()");
					System.exit(1);
				}
			}
		}
	}

	/*メッセージを受信するためのスレッドクラス。
	* run()の中身が、SendThreadのrun()と並列にずっと実行されます。*/
	public static class ReadThread extends Thread {
		private int n = 0;
		private final byte[] buff = new byte[1024];

		@Override
		public void run() {
			/*ここのwhile文は第8回のものとほぼ同じです。*/
			while (true) {
				/*サーバーからの入力受付。サーバーから何かしらデータが送信されるまで、「n=inStr.read(buff);」の行で実行は休止する。
				* サーバーがクライアントにwrite()したらデータが送られてきて、buffにデータが入って、次の行に実行が進む*/
				try {
					n = inStr.read(buff);
				} catch (IOException e) {
					System.out.println("Error by Stream read()");
					System.exit(1);
				}
				//サーバーから送られてきたデータをまるっと文字列に変換する
				String readStr=new String(Arrays.copyOfRange(buff,0,n));
				//文字列が「.disconnect」だったらプログラムを終了する
				if (readStr.equals(".disconnect")) {
					System.out.println("Bye.");
					System.exit(0);
				}
				//コンソールに文字列を表示
				System.out.write(buff, 0, n);
			}
		}
	}
}
