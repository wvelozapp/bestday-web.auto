package com.applause.auto.test;

import com.applause.auto.framework.pageframework.util.logger.LogController;
import com.applause.auto.pageframework.chunks.SignInChunk;
import com.applause.auto.pageframework.pages.LandingPage;
import com.applause.auto.pageframework.pages.LogInPage;
import com.applause.auto.pageframework.utils.Helper;
import com.applause.auto.test.testdata.TestData;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.lang.invoke.MethodHandles;

public class TestSignInWithResults extends BestDayBaseWebdriverTest {

	private static final LogController logger = new LogController(MethodHandles.lookup().lookupClass());

	LandingPage landingPage = null;

	@BeforeMethod
	public void beforeMethod() {
		Helper.maximizeBrowser(getDriver());
	}

	@Test(description = "BEST-POC-1")
	public void TestSignInWithResults() {
		logger.info(String.format("Step 1. Ingresar a %s", TestData.getHomePageUrl()));
		LogInPage logInPage = navigateToLogInPage();

		logger.info("Expected 1. Se muestra la aplicación web de pruebas");
		Assert.assertNotNull(logInPage, "La pagina de LogIn no se muestra");

		SignInChunk signInChunk = logInPage.setupSignInChunk();

		logger.info("Step 2. Introducir el " + TestData.getAccountUsername() + " en el campo usuario");
		logger.info("Step 3. Introducir " + TestData.getAccountPassword() + " en el campo contraseña");

		logInPage.fillBasicInfo(
				TestData.getAccountUsername(),
				TestData.getAccountPassword());

		logger.info("Expected 2. The user profile page displays");
		Assert.assertFalse(signInChunk.isFieldEmpty("Username"), "No se ha llenado el campo Username");
		logger.info("Expected 3. The user profile page displays");
		Assert.assertFalse(signInChunk.isFieldEmpty("Password"), "No se ha llenado el campo Pasword");

		logger.info("Step 4. Click en combobox Dominio");
		signInChunk.triggerDomain();
		logger.info("Expected 4. Muestra el listado con los valores");
		Assert.assertTrue(signInChunk.getDomainList().isDisplayed());

		logger.info("Step 5. Click en 'dominio'");
		signInChunk.pickDomain(TestData.getAccountDomain());
		logger.info("Expected 5. Muestra el valor 'dominio' en el campo Dominio");
		Assert.assertEquals(signInChunk.getFieldText("Domain"), TestData.getAccountDomain() ,"No se ha llenado el campo Dominio");

		logger.info("Step 6. Click en combobox Idioma");
		signInChunk.triggerLanguage();
		logger.info("Expected 6. Muestra el listado con los valores");
		Assert.assertTrue(signInChunk.getLanguageList().isDisplayed());

		logger.info("Step 7. Click en 'idioma'");
		signInChunk.pickLanguage(TestData.getAccountLanguage());
		logger.info("Expected 7. Muestra el valor 'idioma' en el campo Idioma");
		Assert.assertEquals(signInChunk.getFieldText("Language"), TestData.getAccountLanguage(),"No se ha llenado el campo Idioma del Sistema");

		logger.info("Step 8. Click en botón Ingresar");
		landingPage = signInChunk.submitLogin();
		logger.info("Expected 8. Muestra la ventana de filtros desplegada");
		Assert.assertNotNull(landingPage, "Usuario no loggeado o pagina de filtros no cargada");

		logger.info("Step 9. Introducir el valor 'destino' en el campo Destino");
		landingPage.setDestination(TestData.getDestination());
		logger.info("Expected 9. Muestra el valor 'destino' en el campo Destino");
		Assert.assertEquals(landingPage.getDestination().getText(), TestData.getDestination(), "No se ha llenado el campo Destino");

		logger.info("Step 10. Click en el campo Fecha de cancelación");
		landingPage.setCancellationDate(TestData.getCancelDate());
		logger.info("Expected 10. Muestra calendario con fechas");
		// Pending

		logger.info("Step 11. Seleccionar la fecha 'fechaCancelacion'");
		// Pending
		logger.info("Expected 11. Muestra la 'fechaCancelacion' en el campo Fecha de Cancelación");
		Assert.assertEquals(landingPage.getCancellationDate().getText(), TestData.getCancelDate(), "No se ha llenado el campo Fecha de Cancelación");

		logger.info("Step 12. Click en el botón 'Filtrar'");
		landingPage.filterSearchTerms();
		logger.info("Expected 12. Se muestran los resultados de la búsqueda");
		Assert.assertTrue(landingPage.isSearchResultContainerVisible(), "Resultados no filtrados o no procesados.");

		logger.info("Step 13. Verificar que se muestra el mensaje 'Mostrando X registros encontrados' X = Reservas + Prereservas");
		logger.info("Expected 13. Se muestra el valor de X = Reservas + Pre Reservas");
		// Pending
	}
}
