package ritaja.xchangerate;

import java.math.BigDecimal;

import org.json.JSONException;
import org.junit.BeforeClass;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.ritaja.xchangerate.api.CurrencyConverter;
import com.ritaja.xchangerate.api.CurrencyConverterBuilder;
import com.ritaja.xchangerate.api.CurrencyNotSupportedException;
import com.ritaja.xchangerate.endpoint.EndpointException;
import com.ritaja.xchangerate.service.ServiceException;
import com.ritaja.xchangerate.storage.StorageException;
import com.ritaja.xchangerate.util.Currency;
import com.ritaja.xchangerate.util.Strategy;

/**
 * Created by rsengupta on 07/09/15.
 */
@Test
public class ConversionTest {
	private CurrencyConverter converter;

	@BeforeClass
	public void setup() {
		converter = new CurrencyConverterBuilder()
				.strategy(Strategy.YAHOO_FINANCE_FILESTORE)
				.accessKey("")
				.buildConverter();
		converter.setRefreshRateSeconds(86400);
	}

	@Test
	public void simpleConverterFunctionalityTest() throws ServiceException, StorageException, CurrencyNotSupportedException, EndpointException, JSONException {
		Assert.assertNotNull(converter.convertCurrency(new BigDecimal("100"), Currency.USD, Currency.EUR), "Expected a value after conversion");
	}

	@Test
	public void simpleConvertersionTest() throws ServiceException, StorageException, CurrencyNotSupportedException, EndpointException, JSONException {
		Assert.assertTrue(converter.convertCurrency(new BigDecimal("100"), Currency.USD, Currency.GBP).compareTo(new BigDecimal("100.00")) == -1, "Expected a value after conversion");
	}
}
