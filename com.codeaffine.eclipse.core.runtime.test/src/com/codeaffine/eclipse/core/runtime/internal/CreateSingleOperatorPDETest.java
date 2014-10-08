package com.codeaffine.eclipse.core.runtime.internal;

import static com.codeaffine.eclipse.core.runtime.Predicates.alwaysFalse;
import static com.codeaffine.eclipse.core.runtime.Predicates.alwaysTrue;
import static com.codeaffine.eclipse.core.runtime.Predicates.attribute;
import static com.codeaffine.eclipse.core.runtime.TestExtension.EXTENSION_POINT;
import static com.codeaffine.eclipse.core.runtime.ThrowableCaptor.thrown;
import static com.codeaffine.eclipse.core.runtime.internal.ContributionFinder.ERROR_TOO_MANY_CONTRIBUTIONS;
import static com.codeaffine.eclipse.core.runtime.internal.ContributionFinder.ERROR_ZERO_CONTRIBUTIONS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.junit.Before;
import org.junit.Test;

import com.codeaffine.eclipse.core.runtime.ExtensionException;
import com.codeaffine.eclipse.core.runtime.ExtensionExceptionHandler;
import com.codeaffine.eclipse.core.runtime.FindException;
import com.codeaffine.eclipse.core.runtime.TestExtension;
import com.codeaffine.eclipse.core.runtime.TestExtensionConfigurator;
import com.codeaffine.eclipse.core.runtime.ThrowableCaptor.Actor;

public class CreateSingleOperatorPDETest {

  private CreateSingleOperator<TestExtension> operator;

  @Before
  public void setUp() {
    IExtensionRegistry registry = Platform.getExtensionRegistry();
    operator = new CreateSingleOperator<TestExtension>( registry, TestExtension.class );
  }

  @Test
  public void create() {
    equipOperatorWithExtensionPointAndPredicate();

    TestExtension actual = operator.create();

    assertThat( actual ).isNotNull();
  }

  @Test
  public void createWithConfiguration() {
    equipOperatorWithExtensionPointAndPredicate();
    operator.setConfigurator( new TestExtensionConfigurator() );

    TestExtension actual = operator.create();

    assertThat( actual.getId() ).isEqualTo( "1" );
  }

  @Test
  public void createWithDefaultPredicate() {
    equipOperationWithExtensionPoint();

    Throwable actual = thrown( new Actor() {
      @Override
      public void act() throws Throwable {
        operator.create();
      }
    } );

    assertThat( actual )
      .isInstanceOf( FindException.class )
      .hasMessage( ERROR_TOO_MANY_CONTRIBUTIONS );
  }

  @Test
  public void createkOnExceptionWithDefaultHandler() {
    equipOperatorWithExtensionPointAndPredicate();
    equipOperatorWithProblemCausingTypeAttribute();

    Throwable actual = thrown( new Actor() {
      @Override
      public void act() throws Throwable {
        operator.create();
      }
    } );

    assertThat( actual )
      .isInstanceOf( ExtensionException.class )
      .hasCauseInstanceOf( CoreException.class );
  }

  @Test
  public void createkOnExceptionWithCustomHandler() {
    equipOperatorWithExtensionPointAndPredicate();
    equipOperatorWithProblemCausingTypeAttribute();
    ExtensionExceptionHandler exceptionHandler = mockExceptionHandler();
    operator.setExceptionHandler( exceptionHandler );

    TestExtension actual = operator.create();

    assertThat( actual ).isNull();
    verify( exceptionHandler ).handle( any( CoreException.class ) );
  }

  @Test
  public void setPredicateWithTooManyContributions() {
    equipOperationWithExtensionPoint();

    Throwable actual = thrown( new Actor() {
      @Override
      public void act() throws Throwable {
        operator.setPredicate( alwaysTrue() );
      }
    } );

    assertThat( actual )
      .isInstanceOf( FindException.class )
      .hasMessage( ERROR_TOO_MANY_CONTRIBUTIONS );
  }

  @Test
  public void setPredicateWithZeroContributions() {
    equipOperationWithExtensionPoint();

    Throwable actual = thrown( new Actor() {
      @Override
      public void act() throws Throwable {
        operator.setPredicate( alwaysFalse() );
      }
    } );

    assertThat( actual )
      .isInstanceOf( FindException.class )
      .hasMessage( ERROR_ZERO_CONTRIBUTIONS );
  }

  private void equipOperatorWithExtensionPointAndPredicate() {
    equipOperationWithExtensionPoint();
    equipOperatorWithPredicate();
  }

  private void equipOperationWithExtensionPoint() {
    operator.setExtensionPointId( EXTENSION_POINT );
  }

  private void equipOperatorWithPredicate() {
    operator.setPredicate( attribute( "id", "1" ) );
  }

  private static ExtensionExceptionHandler mockExceptionHandler() {
    return mock( ExtensionExceptionHandler.class );
  }

  private void equipOperatorWithProblemCausingTypeAttribute() {
    operator.setTypeAttribute( "unknown" );
  }
}