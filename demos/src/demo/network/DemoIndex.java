package demo.network;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;


public class DemoIndex extends BaseActivity{

	
	@Override
	public void onCreate(Bundle bundle){
		super.onCreate(bundle);
		setContentView(R.layout.main);

        Button demo1 = (Button)this.findViewById(R.id.demo1);
        Button demo2 = (Button)this.findViewById(R.id.demo2);
        Button demo3 = (Button)this.findViewById(R.id.demo3);
        Button demo4 = (Button)this.findViewById(R.id.demo4);
        demo1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(DemoIndex.this, UploadActivity.class);
                startActivity(intent);
            }
        });
        demo2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(DemoIndex.this, GzipActivity.class);
                startActivity(intent);

            }
        });
        demo3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(DemoIndex.this, BasicAuthActivity.class);
                startActivity(intent);
            }
        });

        demo4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(DemoIndex.this, DownloadActivity.class);
                startActivity(intent);
            }
        });
    }
}