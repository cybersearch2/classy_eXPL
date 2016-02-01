/**
    Copyright (C) 2014  www.cybersearch2.com.au

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/> */
package au.com.cybersearch2.telegen;

import static org.fest.assertions.api.Assertions.assertThat;

import java.util.concurrent.Callable;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.Shadows;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.shadows.ShadowDialog;
import org.robolectric.util.SimpleFuture;

import android.content.Context;
import android.os.SystemClock;
import android.support.v4.content.AsyncTaskLoader;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView;
import au.com.cybersearch2.classywidget.ListItem;
import au.com.cybersearch2.telegen.entity.TestChecks;
import au.com.cybersearch2.telegen.entity.TestIssues;

/**
 * MainActivityTest
 * @author Andrew Bowley
 * 14/05/2014
 */
@RunWith(RobolectricTestRunner.class)
public class MainActivityTest
{
    @Implements(value = SystemClock.class, callThroughByDefault = true)
    public static class MyShadowSystemClock {
        public static long elapsedRealtime() {
            return 0;
        }
    }

    @Implements(AsyncTaskLoader.class)
    public static class MyShadowAsyncTaskLoader<D> 
    {
          @RealObject private AsyncTaskLoader<D> realLoader;
          private SimpleFuture<D> future;

          public void __constructor__(Context context) {
            BackgroundWorker worker = new BackgroundWorker();
            future = new SimpleFuture<D>(worker) {
              @Override protected void done() {
                try {
                  final D result = get();
                  Robolectric.getForegroundThreadScheduler().post(new Runnable() {
                    @Override public void run() {
                      realLoader.deliverResult(result);
                    }
                  });
                } catch (InterruptedException e) {
                  // Ignore
                }
              }
            };
          }

          @Implementation
          public void onForceLoad() {
              Robolectric.getBackgroundThreadScheduler().post(new Runnable() {
              @Override
              public void run() {
                future.run();
              }
            });
          }

          private final class BackgroundWorker implements Callable<D> {
            @Override public D call() throws Exception {
              return realLoader.loadInBackground();
            }
          }
    }
   
    public static final long ID = 1L;
    static String DATABASE_NAME = "telegen.db";

    @Before
    public void setUp() 
    {
    }

    @After
    public void tearDown() 
    {
    }
    

    @Config(shadows = { MyShadowSystemClock.class, MyShadowAsyncTaskLoader.class })
    @Test
    public void test_OnCreate() throws Exception
    {
        MainActivity mainActivity = Robolectric.buildActivity(MainActivity.class).create().start().visible().get();
        assertThat(mainActivity.telegenLogic).isNotNull();
        assertThat(mainActivity.adapter.getCount()).isEqualTo(TestIssues.ISSUE_DATA.length);
        int index = 0;
        for (int i = 0; i < TestIssues.ISSUE_DATA.length; i++)
        {
            ListItem value = (ListItem)mainActivity.adapter.getItem(i);
            String[] issueItem = TestIssues.ISSUE_DATA[index++];
            assertThat(value.getName()).isEqualTo(issueItem[0]);
            assertThat(value.getValue()).isEqualTo(issueItem[1]);
        }
        assertThat(mainActivity.displayListFragment.isVisible()).isTrue();
        assertThat(mainActivity.displayListFragment.getListView().getCount()).isEqualTo(TestIssues.ISSUE_DATA.length);
        OnItemClickListener onItemClickListener = mainActivity.displayListFragment.getListView().getOnItemClickListener();
        Robolectric.getBackgroundThreadScheduler().advanceToLastPostedRunnable();
        Robolectric.getForegroundThreadScheduler().pause();
        // position = 0
        onItemClickListener.onItemClick(null, null, 0, 0);
        Robolectric.getBackgroundThreadScheduler().runOneTask();
        assertThat(mainActivity.telegenLogic.currentCheck).isEqualTo(TestChecks.CHECK_DATA[0][0]);
        Robolectric.getForegroundThreadScheduler().advanceToLastPostedRunnable();
        
        ShadowDialog dialog = Shadows.shadowOf(ShadowDialog.getLatestDialog());
        assertThat(dialog.getTitle()).isEqualTo(DisplayDetailsDialog.DIALOG_TITLE);
        assertThat(dialog.isCancelableOnTouchOutside()).isTrue();
        TextView tv1 = (TextView) ShadowDialog.getLatestDialog().findViewById(R.id.detail_title);
        assertThat(tv1.getText()).isEqualTo(TestIssues.ISSUE_DATA[0][0]);
        TextView tv2 = (TextView) ShadowDialog.getLatestDialog().findViewById(R.id.detail_context);
        assertThat(tv2.getText()).isEqualTo(TestIssues.ISSUE_DATA[0][1]);
        TextView tv3 = (TextView) ShadowDialog.getLatestDialog().findViewById(R.id.detail_content);
        assertThat(tv3.getText()).isEqualTo(TestChecks.CHECK_DATA[0][1]);
        test_NextCheck(mainActivity.telegenLogic);
    }
 
    public void test_NextCheck(TelegenLogic telegenLogic)
    {
        String firstCheck = telegenLogic.getFirstCheck("Start");
        assertThat(firstCheck).isEqualTo("Make sure the AC power cord is securely plugged in to the wall outlet");
        assertThat(telegenLogic.currentCheck).isEqualTo("Power cord");
        String nextCheck = telegenLogic.getNextCheck();
        assertThat(nextCheck).isEqualTo("Make sure the wall outlet is working");
        assertThat(telegenLogic.currentCheck).isEqualTo("Wall outlet");
        nextCheck = telegenLogic.getNextCheck();
        assertThat(nextCheck).isEqualTo("Try pressing the POWER button on the TV to make sure the problem is not the remote.");
        assertThat(telegenLogic.currentCheck).isEqualTo("Remote");
        nextCheck = telegenLogic.getNextCheck();
        assertThat(nextCheck).isEqualTo("Contact Support");
        assertThat(telegenLogic.currentCheck).isEqualTo(TelegenLogic.CALL_SUPPORT);
    }
}
