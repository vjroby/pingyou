package com.requester.pingyou;

import com.requester.pingyou.views.FirstTestView;
import de.felixroske.jfxsupport.AbstractJavaFxApplicationSupport;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class PingyouApplication extends AbstractJavaFxApplicationSupport {

	public static void main(String[] args) {
		launchApp(PingyouApplication.class, FirstTestView.class, args);
	}
}
