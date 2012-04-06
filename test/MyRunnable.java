import java.io.Serializable;

import play.Logger;

public class MyRunnable implements Runnable, Serializable {
	private static final long serialVersionUID = 1L;
	public String msg;

	public MyRunnable(String msg) {
		this.msg = msg;
	}

	public void run() {
		Logger.info("msg: %s", msg);
	}
}