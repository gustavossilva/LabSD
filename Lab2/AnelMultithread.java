import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class AnelMultithread extends Thread {
	private static AnelMultithread[] threads = new AnelMultithread[30];
	private static char[] message = new char[80];
	private static AtomicInteger currentThread;
	private static AtomicBoolean running;
	private int id;

	static {
		Random generator = new Random();

		for (int i = 0; i < message.length; i++) {
			if (generator.nextInt() % 2 == 1)
				message[i] = (char) ('A' + generator.nextInt(26));

			else
				message[i] = (char) ('a' + generator.nextInt(26));
		}

		currentThread = new AtomicInteger(0);
		running = new AtomicBoolean(true);
	}

	public AnelMultithread(int id) {
		this.id = id;
	}

	public void run() {
		boolean found;

		while ( running.get() ) {
			try {
				if (currentThread.get() % threads.length == this.id) {
					System.out.println("Thread " + this.id + ":\t" + new String(message));

					found = false;

					for (int i = 0; !found && (i < message.length); i++) {
						found = (message[i] >= 'a') && (message[i] <= 'z');

						if (found)
							message[i] += ('A' - 'a');
					}

					if (!found)
						running.set(false);

					Thread.currentThread().sleep(1000);
					currentThread.incrementAndGet();
				}

				else
					Thread.currentThread().sleep(100);
			}

			catch (InterruptedException e) {}
		}
	}

	public static void main(String[] args) {
		for (int i = 0; i < threads.length; i++) {
			threads[i] = new AnelMultithread(i);
			threads[i].start();
		}

		for (int i = 0; i < threads.length; i++) {
			try {
				threads[i].join();
			}

			catch (InterruptedException e) {}
		}

		System.out.println("Final mesage:\t" + new String(message));
	}
}
