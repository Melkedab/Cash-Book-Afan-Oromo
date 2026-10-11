package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.CurrencyCode
import com.example.localization.AppLanguage
import com.example.localization.LocalizationManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Galmee Qarshii", appName)
  }

  @Test
  fun `verify Afaan Oromo financial strings`() {
    val stringsOm = LocalizationManager.get(AppLanguage.AFAAN_OROMOO)
    assertEquals("Galmee Qarshii", stringsOm.appName)
    assertEquals("Qarshii Seene (Galii)", stringsOm.cashIn)
    assertEquals("Qarshii Bahe (Baasii)", stringsOm.cashOut)
    assertEquals("Galmee Liqii fi Qabaa", stringsOm.debtTitle)
  }

  @Test
  fun `verify multi-currency conversion defaults`() {
    val usd = CurrencyCode.fromCode("USD")
    val etb = CurrencyCode.fromCode("ETB")
    assertEquals(1.0, etb.defaultRateToETB, 0.001)
    assertTrue(usd.defaultRateToETB > 100.0)
  }
}

