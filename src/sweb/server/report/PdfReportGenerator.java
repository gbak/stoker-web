package sweb.server.report;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

public class PdfReportGenerator
{

    public static final float[][] COLUMNS = {
        { 40, 36, 219, 579 } , { 234, 36, 414, 579 },
        { 428, 36, 608, 579 } , { 622, 36, 802, 579 }
    };
    
    public void createPdf( String filename )

    {
        Document document = new Document();
        // step 2
        PdfWriter writer = null;
        try
        {
            writer = PdfWriter.getInstance(document, new FileOutputStream(filename));
        
            // step 3
            document.open();
            // step 4
            // Create a table and fill it with movies

            //PdfPTable table = new PdfPTable(new float[] { 1, 5, 5, 1});

            Paragraph p1 = new Paragraph();
            p1.add("Log: ");
            p1.add(" Name");
            p1.add(Chunk.NEWLINE);
            p1.add("Duration: " + "12:33:32" + "\n") ;
            //document.add( p1 );
            
            PdfContentByte canvas = writer.getDirectContent();
            canvas.beginText();
            Phrase c1 = new Phrase("Log: " + "logname");
            ColumnText.showTextAligned(canvas, Element.ALIGN_LEFT, c1, 36, 400, 0);
            c1 = new Phrase("Log: " + "logname");
            ColumnText.showTextAligned(canvas, Element.ALIGN_LEFT, c1, 36, 680, 0);
            
            canvas.endText();
            
           /* Paragraph p2 = new Paragraph();
            p2.add("Cooker Name: ");
            p2.add(" Name");
            p2.add("\n");
            p2.add("Total Fan Runtime: " + "12:33:32" + "\n") ;
            p2.add("Total Fan Cycles:  " + "212" + "\n") ;
            p2.setIndentationLeft(80);*/
        
            //document.add(p2);
            /*
            PdfPTable table = new PdfPTable(2);
            table.addCell(p1);
            table.completeRow();
            // set the total width of the table
            table.setTotalWidth(600);
            PdfContentByte canvas = writer.getDirectContentUnder();
            // draw the first three columns on one page
            table.writeSelectedRows(0, 2, 0, -1, 236, 806, canvas);
            document.newPage();
            // draw the next three columns on the next page
            table.writeSelectedRows(2, -1, 0, -1, 36, 806, canvas);
            // step 5
             
             */
            document.close();
        }
        catch (FileNotFoundException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (DocumentException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
    }

    public static void main(String[] args)
    {
        PdfReportGenerator pdf = new PdfReportGenerator();
        pdf.createPdf("C:/tmp/pdfReport.pdf");

    }

}
