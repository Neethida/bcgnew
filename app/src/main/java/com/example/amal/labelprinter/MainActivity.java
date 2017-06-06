package com.example.amal.labelprinter;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.Build;
import android.os.CancellationSignal;
import android.os.Handler;
import android.os.ParcelFileDescriptor;
import android.print.PageRange;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintDocumentInfo;
import android.print.PrintManager;
import android.print.pdf.PrintedPdfDocument;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.EnumMap;
import java.util.GregorianCalendar;
import java.util.Locale;
import  java.util.Map;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

import static com.example.amal.labelprinter.R.id.itemNumber;
import static com.example.amal.labelprinter.R.id.text;

public class MainActivity extends AppCompatActivity {

    Bitmap barcode;
    EditText dateMy;
    EditText itemNumber;
    EditText caseCount;
    EditText skidNumber;
    EditText parallelizer;
    TableLayout tl;
    ImageView imageBc;
    LinearLayout printlayout;
    LinearLayout privewlayout;



    SimpleDateFormat dateF = new SimpleDateFormat("MM/dd/yy", Locale.getDefault());
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        itemNumber =(EditText)findViewById(R.id.itemNumber);
        caseCount = (EditText)findViewById(R.id.caseCount);
        skidNumber = (EditText)findViewById(R.id.skidNumber);
        parallelizer = (EditText)findViewById(R.id.parallelizer);
        dateMy = (EditText)findViewById(R.id.dateMy);

        String dateCurrent = dateF.format(Calendar.getInstance().getTime());
        dateMy.setText(dateCurrent);

        printlayout =(LinearLayout) findViewById(R.id.printlayout);
        printlayout.setVisibility(View.INVISIBLE);

        privewlayout=(LinearLayout) findViewById(R.id.privewlayout);
        privewlayout.setVisibility(View.INVISIBLE);

        dateMy.setOnClickListener(new View.OnClickListener() {

            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View v) {
                Calendar myCurrentDate = Calendar.getInstance();
                int mYear = myCurrentDate.get(Calendar.YEAR);
                int mMonth = myCurrentDate.get(Calendar.MONTH);
                int mDay = myCurrentDate.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog mDatePicker;

                mDatePicker = new DatePickerDialog(MainActivity.this, new DatePickerDialog.OnDateSetListener() {
                    public void onDateSet(DatePicker datepicker, int selectedyear, int selectedmonth, int selectedday) {

                        Date date = new GregorianCalendar(selectedyear, selectedmonth, selectedday).getTime();
                        dateMy.setText(dateF.format(date));
                    }
                }, mYear, mMonth, mDay);

                mDatePicker.setTitle("Select Date");
                mDatePicker.show();
            }
        });
        Button  previewBtn = (Button)findViewById(R.id.previewBtn);
        previewBtn.setOnClickListener(new View.OnClickListener() {

            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View v) {

                privewlayout.removeAllViews();
                GenerateBarcode(GetBarcodeText());
                layoutContentent(true);
                privewlayout.setVisibility(View.VISIBLE);
            }
        });
        Button  printBtn = (Button)findViewById(R.id.printBtn);
        printBtn.setOnClickListener(new View.OnClickListener() {

            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View v) {


                if(itemNumber.getText().toString().length() != 5 || TextUtils.isEmpty(itemNumber.getText().toString())) {
                    itemNumber.setError("Enter Item number");
                    return;
                }
                else if(caseCount.getText().toString().length() != 3 || TextUtils.isEmpty(caseCount.getText().toString())){
                    caseCount.setError("Enter Case count");
                    return;
                }
                else if(skidNumber.getText().toString().length() != 2 || TextUtils.isEmpty(skidNumber.getText().toString())){
                    skidNumber.setError("Enter Skid number");
                    return;
                }
                else if(parallelizer.getText().toString().length() != 2 || TextUtils.isEmpty(parallelizer.getText().toString())){
                    parallelizer.setError("Enter Parallelizer initials");
                    return;
                }
                else if(TextUtils.isEmpty(dateMy.getText().toString())){
                    dateMy.setError("Enter Date");
                    return;
                }
                else{
                    GenerateBarcode(GetBarcodeText());
                    layoutContentent(false);
                    PrintBarcode();
                }

            }
        });




    }

    private  void PrintBarcode()
    {

        PrintManager printManager = (PrintManager)
                this.getSystemService(Context.PRINT_SERVICE);


        GenericPrintAdapter printAdapter =new GenericPrintAdapter(this,printlayout);

        printManager.print("MyPrintJob",printAdapter,null).isCompleted();


    }
public class GenericPrintAdapter extends PrintDocumentAdapter{

    View view;
    Context context;
    private int totalPages;
    PrintedPdfDocument document;
    private View mView;

    public GenericPrintAdapter(Context context, View view){

        this.view = view;
        this.context = context;
        totalPages = 3;
        mView = view;
    }
    @Override
    public void onLayout(PrintAttributes oldAttributes, PrintAttributes newAttributes,
                         CancellationSignal cancellationSignal, LayoutResultCallback callback, Bundle extras) {

        document = new PrintedPdfDocument(context,newAttributes);
        if(totalPages>0){

            PrintDocumentInfo printDocumentInfo = new PrintDocumentInfo
                    .Builder("MyPrint.pdf")
                    .setContentType(PrintDocumentInfo.CONTENT_TYPE_UNKNOWN)
                    .setPageCount(totalPages)
                   .build();

            callback.onLayoutFinished(printDocumentInfo, true);
        }
        {
            callback.onLayoutFailed("Page count calculation failed.");
        }

    }
    @Override
    public void onWrite(PageRange[] pages, ParcelFileDescriptor destination,
                        CancellationSignal cancellationSignal, WriteResultCallback callback) {

        for (int i = 0; i < totalPages; i++)
        {
            PrintedPdfDocument.Page page = document.startPage(i);
            Bitmap bitmap = Bitmap.createBitmap(mView.getWidth(), mView.getHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            mView.draw(canvas);
            Rect src = new Rect(0, 0, mView.getWidth(), mView.getHeight());
            Canvas pageCanvas = page.getCanvas();

            float pageWidth = pageCanvas.getWidth();
            float pageHeight = pageCanvas.getHeight();

            float scale = Math.min(pageWidth / src.width(), pageHeight / src.height());
            float left = pageWidth / 2 - src.width() * scale / 2;
            float top = pageHeight / 2 ;
            float right = pageWidth / 2 + src.width() * scale / 2;
            float bottom = pageHeight / 2 + src.height() * scale ;
            RectF dst = new RectF(left, top, right, bottom);

            pageCanvas.drawBitmap(bitmap, src, dst, null);
            document.finishPage(page);
        }

        WritePrintedPdfDoc(destination);

        document.close();

        document = null;

        PageRange[] writtenPages = new PageRange[1];
        writtenPages[0] = new PageRange(1, 3);

        callback.onWriteFinished(pages);

    }
    void WritePrintedPdfDoc(ParcelFileDescriptor destination)
    {
        try {
            java.io.FileOutputStream fileOutputStream = new java.io.FileOutputStream(destination.getFileDescriptor());
            document.writeTo(fileOutputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @Override
    public void onFinish() {
        super.onFinish();
        skidNumber.getText().clear();
        parallelizer.getText().clear();


        printlayout.removeAllViews();
        privewlayout.setVisibility(View.GONE);
        privewlayout.setVisibility(View.GONE);

    }

}


    private Bitmap GenerateBarcode(String text){



        // barcode image
        Bitmap bitmap = null;

        try {

            bitmap = encodeAsBitmap(GetBarcodeText(), BarcodeFormat.CODE_128, 1200, 250);

        } catch (WriterException e) {
            e.printStackTrace();
        }
        barcode = bitmap;

        float scale = getResources().getDisplayMetrics().density;
        Canvas canvas = new Canvas(barcode);
        Paint paint =new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        paint.setTextSize(20*scale);
        Rect bounds =new Rect();
        paint.getTextBounds(text,0,text.length(),bounds);
        int x = (barcode.getWidth()-bounds.width())/2;
        int y = (barcode.getHeight()-bounds.height());
        paint.setColor(Color.rgb(250,250,250));
        canvas.drawRect(0,y-15,barcode.getWidth(),barcode.getHeight(),paint);
        paint.setColor(Color.rgb(0,0,0));
        //paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        canvas.drawText(text,x,y+bounds.height()-15,paint);
        return barcode;

    }

    public void layoutContentent(boolean layoutType){


        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
        LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

        layoutParams.setMargins(45,0,45,0);

        tl = new TableLayout(this);
        tl.setLayoutParams(printlayout.getLayoutParams());
        tl.setLayoutParams(layoutParams);
        tl.setBackgroundColor(Color.BLACK);

        this.GenerateBarcode(GetBarcodeText());
        imageBc =new ImageView(this);
        imageBc.setImageBitmap(barcode);

        if (layoutType) {
            privewlayout.addView(imageBc);
            privewlayout.addView(tl);
        }else {
            printlayout.addView(imageBc);
            printlayout.addView(tl);
        }


        TableLayout.LayoutParams lp =new TableLayout.LayoutParams(

        );
        lp.setMargins(1,1,1,1);

        TableRow row1 = new TableRow(this);
        TableRow row2 = new TableRow(this);
        TableRow row3 = new TableRow(this);
        row1.setLayoutParams(lp);
        row2.setLayoutParams(lp);
        row3.setLayoutParams(lp);

        TableRow.LayoutParams lp2 = new TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT,
                TableRow.LayoutParams.MATCH_PARENT
        );
        lp2.weight=1;
        lp2.setMargins(2,2,2,2);


        TextView tv11 = new TextView(this);
        tv11.setTypeface(null, Typeface.BOLD);
        tv11.setBackgroundColor(Color.WHITE);
        tv11.setTextColor(Color.BLACK);
        tv11.setGravity(Gravity.CENTER);
        tv11.setLayoutParams(lp2);
        tv11.setText("Item#");

        TextView tv12 = new TextView(this);
        tv12.setBackgroundColor(Color.WHITE);
        tv12.setGravity(Gravity.CENTER);
        tv12.setTextSize(25);
        tv12.setTextColor(Color.BLACK);
        tv12.setLayoutParams(lp2);
        tv12.setText(itemNumber.getText());

        TextView tv21 = new TextView(this);
        tv21.setBackgroundColor(Color.WHITE);
        tv21.setTypeface(null, Typeface.BOLD);
        tv21.setGravity(Gravity.CENTER);
        tv21.setLayoutParams(lp2);
        tv21.setTextColor(Color.BLACK);
        tv21.setText("Case Count");

        TextView tv22 = new TextView(this);
        tv22.setBackgroundColor(Color.WHITE);
        tv22.setTextColor(Color.BLACK);
        tv22.setGravity(Gravity.CENTER);
        tv22.setLayoutParams(lp2);
        tv22.setTextSize(25);
        tv22.setText(caseCount.getText());

        TextView tv23 = new TextView(this);
        tv23.setLayoutParams(lp2);
        tv23.setGravity(Gravity.CENTER);
        tv23.setBackgroundColor(Color.WHITE);
        tv23.setTextColor(Color.BLACK);
        tv23.setTypeface(null, Typeface.BOLD);
        tv23.setText("Skid Count");

        TextView tv24 = new TextView(this);
        tv24.setBackgroundColor(Color.WHITE);
        tv24.setTextColor(Color.BLACK);
        tv24.setGravity(Gravity.CENTER);
        tv24.setLayoutParams(lp2);
        tv24.setTextSize(25);
        tv24.setText(skidNumber.getText());

        TextView tv31 = new TextView(this);
        tv31.setBackgroundColor(Color.WHITE);
        tv31.setTypeface(null, Typeface.BOLD);
        tv31.setGravity(Gravity.CENTER);
        tv31.setTextColor(Color.BLACK);
        tv31.setLayoutParams(lp2);
        tv31.setText("Prod Date");

        TextView tv32 = new TextView(this);
        tv32.setText(dateMy.getText());
        tv32.setTextSize(25);
        tv32.setGravity(Gravity.CENTER);
        tv32.setLayoutParams(lp2);
        tv32.setTextColor(Color.BLACK);
        tv32.setBackgroundColor(Color.WHITE);

        TextView tv33 = new TextView(this);
        tv33.setTypeface(null, Typeface.BOLD);
        tv33.setBackgroundColor(Color.WHITE);
        tv33.setGravity(Gravity.CENTER);
        tv33.setTextColor(Color.BLACK);
        tv33.setLayoutParams(lp2);
        tv33.setText("Palletizers initial");

        TextView tv34 = new TextView(this);
        tv34.setBackgroundColor(Color.WHITE);
        tv34.setTextColor(Color.BLACK);
        tv34.setGravity(Gravity.CENTER);
        tv34.setLayoutParams(lp2);
        tv34.setTextSize(25);
        tv34.setText(parallelizer.getText());



        tl.addView(row1);
        tl.addView(row2);
        tl.addView(row3);

        row1.addView(tv11);
        row1.addView(tv12);

        row2.addView(tv21);
        row2.addView(tv22);
        row2.addView(tv23);
        row2.addView(tv24);

        row3.addView(tv31);
        row3.addView(tv32);
        row3.addView(tv33);
        row3.addView(tv34);
    }



    private  String GetBarcodeText()
    {
        return dateMy.getText().toString() +" "+itemNumber.getText().toString() +" "+ caseCount.getText().toString() +" "+ skidNumber.getText().toString() ;
    }







    private static final int WHITE = 0xFFFFFFFF;
    private static final int BLACK = 0xFF000000;

    Bitmap encodeAsBitmap(String contents, BarcodeFormat format, int img_width, int img_height) throws WriterException {
        String contentsToEncode = contents;
        if (contentsToEncode == null) {
            return null;
        }
        Map<EncodeHintType, Object> hints = null;
        String encoding = guessAppropriateEncoding(contentsToEncode);
        if (encoding != null) {
            hints = new EnumMap<EncodeHintType, Object>(EncodeHintType.class);
            hints.put(EncodeHintType.CHARACTER_SET, encoding);
        }
        MultiFormatWriter writer = new MultiFormatWriter();
        BitMatrix result;
        try {
            result = writer.encode(contentsToEncode, format, img_width, img_height, hints);
        } catch (IllegalArgumentException iae) {
            // Unsupported format
            return null;
        }
        int width = result.getWidth();
        int height = result.getHeight();
        int[] pixels = new int[width * height];
        for (int y = 0; y < height; y++) {
            int offset = y * width;
            for (int x = 0; x < width; x++) {
                pixels[offset + x] = result.get(x, y) ? BLACK : WHITE;
            }
        }

        Bitmap bitmap = Bitmap.createBitmap(width, height,
                Bitmap.Config.ARGB_8888);
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
        return bitmap;
    }

    private static String guessAppropriateEncoding(CharSequence contents) {
        // Very crude at the moment
        for (int i = 0; i < contents.length(); i++) {
            if (contents.charAt(i) > 0xFF) {
                return "UTF-8";
            }
        }
        return null;
    }

}


