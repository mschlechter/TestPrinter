import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.print.*;

import javax.print.*;
import javax.print.attribute.*;
import javax.print.attribute.standard.*;
import javax.print.event.*;

public class TestPrinter {

	public static void main(String[] args) {

		// Toon beschikbare printers
		PrintService[] printServices = PrintServiceLookup.lookupPrintServices(null, null);
		System.out.println("Number of print services: " + printServices.length);

		for (PrintService printer : printServices) {
			System.out.println("Printer: " + printer.getName());

			boolean a4supported = printer.isAttributeValueSupported(MediaSizeName.ISO_A4, null, null);
			if (a4supported) {
				System.out.println("A4 paper size supported");
			}

			// PrintRequestAttributeSet kun je vullen als je gaat printen
			// Hier kun je dus de paper size doorgeven of de media tray
			printSomething(printer);
		}
	}

	private static void printSomething(PrintService printer) {
		PrintRequestAttributeSet aset = new HashPrintRequestAttributeSet();
		aset.add(OrientationRequested.LANDSCAPE);
		aset.add(new JobName("Hello", null));

		try {
			DocFlavor flavor = DocFlavor.SERVICE_FORMATTED.PRINTABLE;
			Doc doc = new SimpleDoc(new HelloWorldPrinter(), flavor, null);

			DocPrintJob job = printer.createPrintJob();

			PrintJobWatcher pjw = new PrintJobWatcher(job);
			job.print(doc, aset);
			pjw.waitForDone();
		} catch (Exception e) {
			System.err.println(e);
		}
	}
}

class PrintJobWatcher {
	boolean done = false;

	PrintJobWatcher(DocPrintJob job) {
		job.addPrintJobListener(new PrintJobAdapter() {
			public void printJobCanceled(PrintJobEvent pje) {
				allDone();
			}

			public void printJobCompleted(PrintJobEvent pje) {
				allDone();
			}

			public void printJobFailed(PrintJobEvent pje) {
				allDone();
			}

			public void printJobNoMoreEvents(PrintJobEvent pje) {
				allDone();
			}

			void allDone() {
				synchronized (PrintJobWatcher.this) {
					done = true;
					System.out.println("Printing done ...");
					PrintJobWatcher.this.notify();
				}
			}
		});
	}

	public synchronized void waitForDone() {
		try {
			while (!done) {
				wait();
			}
		} catch (InterruptedException e) {
		}
	}
}

class HelloWorldPrinter implements Printable {
	@Override
	public int print(Graphics g, PageFormat pf, int page) throws PrinterException {

		if (page > 0) {
			return NO_SUCH_PAGE;
		}

		System.out.println(pf.getWidth());

		Graphics2D g2d = (Graphics2D) g;
		g2d.translate(pf.getImageableX(), pf.getImageableY());

		g.drawString("Hello world!", 100, 100);

		return PAGE_EXISTS;
	}
}
