package com.xchangerate.api.conversiontest;

import static java.lang.Math.abs;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.Properties;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.ritaja.xchangerate.api.CurrencyNotSupportedException;
import com.ritaja.xchangerate.api.XchangeRateException;
import com.ritaja.xchangerate.api.YahooCurrencyEndpoint;
import com.ritaja.xchangerate.util.Currency;
import com.tunyk.currencyconverter.BankUaCom;
import com.tunyk.currencyconverter.api.CurrencyConverterException;

/**
 * Created by rsengupta on 26/08/15.
 */
@Test(groups = {"ConversionTests"})
public class YahooCurrencyEndpointTest {
	private YahooCurrencyEndpoint yahooCurrencyEndpoint;
	private com.tunyk.currencyconverter.api.CurrencyConverter currencyConverter;
	private Properties properties = new Properties();
	private InputStream inputStream;

	@BeforeClass
	public void setup() throws XchangeRateException, CurrencyConverterException, IOException {
		inputStream = new FileInputStream("src/test/resources/access_key.properties");
		properties.load(inputStream);
		yahooCurrencyEndpoint = new YahooCurrencyEndpoint();
		// setup external library for test
		currencyConverter = new BankUaCom(com.tunyk.currencyconverter.api.Currency.USD, com.tunyk.currencyconverter.api.Currency.EUR);
		Assert.assertNotNull(currencyConverter, "Expected properly configured external helper library");
	}

	@Test(enabled = true)
	public void convertFromUSDTest() throws XchangeRateException, CurrencyConverterException, CurrencyNotSupportedException {
		yahooCurrencyEndpoint.setRefreshrateSeconds(86400);
		double refConversion = (double) currencyConverter.convertCurrency(100f, com.tunyk.currencyconverter.api.Currency.USD, com.tunyk.currencyconverter.api.Currency.EUR);
		double actualCoversion = yahooCurrencyEndpoint.convertCurrency(new BigDecimal(100.00), Currency.USD, Currency.EUR).doubleValue();
		Assert.assertTrue(abs(refConversion - actualCoversion) < 3.00, "Expected conversion accuracy of 3.00 units");
	}

	@Test(enabled = true)
	public void convertToUSDTest() throws XchangeRateException, CurrencyConverterException, CurrencyNotSupportedException {
		yahooCurrencyEndpoint.setRefreshrateSeconds(86400);
		double refConversion = (double) currencyConverter.convertCurrency(100f, com.tunyk.currencyconverter.api.Currency.EUR, com.tunyk.currencyconverter.api.Currency.USD);
		double actualCoversion = yahooCurrencyEndpoint.convertCurrency(new BigDecimal(100.00), Currency.EUR, Currency.USD).doubleValue();
		Assert.assertTrue(abs(refConversion - actualCoversion) < 3.00, "Expected conversion accuracy of 3.00 units");
	}

	@Test(enabled = true)
	public void convertCurrencyTest() throws XchangeRateException, CurrencyConverterException, CurrencyNotSupportedException {
		yahooCurrencyEndpoint.setRefreshrateSeconds(86400);
		double refConversion = (double) currencyConverter.convertCurrency(100f, com.tunyk.currencyconverter.api.Currency.CAD, com.tunyk.currencyconverter.api.Currency.EUR);
		double actualCoversion = yahooCurrencyEndpoint.convertCurrency(new BigDecimal(100.00), Currency.CAD, Currency.EUR).doubleValue();
		Assert.assertTrue(abs(refConversion - actualCoversion) < 3.00, "Expected conversion accuracy of 3.00 units");
	}

	@Test(enabled = true)
	public void convertToSameCurrencyTest() throws XchangeRateException, CurrencyConverterException, CurrencyNotSupportedException {
		yahooCurrencyEndpoint.setRefreshrateSeconds(86400);
		double refConversion = (double) currencyConverter.convertCurrency(100f, com.tunyk.currencyconverter.api.Currency.EUR, com.tunyk.currencyconverter.api.Currency.EUR);
		double actualCoversion = yahooCurrencyEndpoint.convertCurrency(new BigDecimal(100.00), Currency.EUR, Currency.EUR).doubleValue();
		Assert.assertTrue(abs(refConversion - actualCoversion) < 3.00, "Expected conversion accuracy of 3.00 units");
	}
}
