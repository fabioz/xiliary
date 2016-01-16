package com.codeaffine.eclipse.swt.widget.scrollable;

import static com.codeaffine.eclipse.swt.util.ControlReflectionUtil.$;
import static com.codeaffine.eclipse.swt.util.ControlReflectionUtil.CREATE_WIDGET;
import static com.codeaffine.eclipse.swt.util.ControlReflectionUtil.DISPLAY;
import static com.codeaffine.eclipse.swt.util.ControlReflectionUtil.PARENT;
import static com.codeaffine.eclipse.swt.util.ControlReflectionUtil.STYLE;
import static com.codeaffine.eclipse.swt.util.Platform.PlatformType.WIN32;
import static java.lang.Integer.valueOf;
import static java.lang.String.format;
import static java.util.Collections.unmodifiableList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Scrollable;

import com.codeaffine.eclipse.swt.util.ControlReflectionUtil;
import com.codeaffine.eclipse.swt.util.Platform;
import com.codeaffine.eclipse.swt.util.PlatformSupport;

public class ScrollableAdapterFactory {

  static final String ADAPTED = ScrollableAdapterFactory.class.getName() + "#adapted";
  static final Collection<Class<?>> SUPPORTED_TYPES = supportedTypes();

  private final ControlReflectionUtil reflectionUtil;
  private final PlatformSupport platformSupport;

  public interface Adapter<S extends Scrollable> {
    void adapt( S scrollable, PlatformSupport platformSupport );
    S getScrollable();
  }

  public ScrollableAdapterFactory() {
    reflectionUtil = new ControlReflectionUtil();
    platformSupport = new PlatformSupport( WIN32 );
  }

  public <S extends Scrollable, A extends Scrollable & Adapter<S>> A create( S scrollable, Class<A> type ) {
    ensureThatTypeIsSupported( type );

    Composite parent = scrollable.getParent();
    int ordinalNumber = captureDrawingOrderOrdinalNumber( scrollable );
    A result = createAdapter( scrollable, type );
    if( platformSupport.isGranted() ) {
      markAdapted( scrollable );
      applyDrawingOrderOrdinalNumber( result, ordinalNumber );
    }
    result.adapt( scrollable, platformSupport );
    if( platformSupport.isGranted() ) {
      parent.layout();
      result.setBackground( scrollable.getBackground() );
      reflectionUtil.setField( scrollable, ControlReflectionUtil.PARENT, parent );
    }
    return result;
  }


  public <S extends Scrollable> void markAdapted( S scrollable ) {
    scrollable.setData( ADAPTED, Boolean.TRUE );
  }

  public boolean isAdapted( Scrollable scrollable ) {
    return Boolean.TRUE.equals( scrollable.getData( ADAPTED ) );
  }

  @SuppressWarnings("unchecked")
  static <T extends Scrollable> LayoutFactory<T> createLayoutFactory(
    Platform platform, LayoutMapping<T> ... mappings )
  {
    for( LayoutMapping<T> layoutMapping : mappings ) {
      if( platform.matchesOneOf( layoutMapping.getPlatformTypes() ) ) {
        return layoutMapping.getLayoutFactory();
      }
    }
    return new NativeLayoutFactory<T>();
  }

  private static <A extends Adapter<? extends Scrollable>> void ensureThatTypeIsSupported( Class<A> type ) {
    if( !SUPPORTED_TYPES.contains( type ) ) {
      throw new IllegalArgumentException( format( "Scrollable type <%s> is not supported.", type ) );
    }
  }

  private static int captureDrawingOrderOrdinalNumber( Control control ) {
    for( int i = 0; i < control.getParent().getChildren().length; i++ ) {
      if( control.getParent().getChildren()[ i ] == control ) {
        return i;
      }
    }
    throw new IllegalStateException( "Control is not contained in its parent's children list: " + control );
  }

  private <S extends Scrollable, A extends Scrollable & Adapter<S>> A createAdapter( S scrollable, Class<A> type ) {
    int style = SWT.BORDER & scrollable.getStyle();
    A result = reflectionUtil.newInstance( type );
    if( platformSupport.isGranted() ) {
      reflectionUtil.setField( result, DISPLAY, Display.getCurrent() );
      reflectionUtil.setField( result, PARENT, scrollable.getParent() );
      reflectionUtil.setField( result, STYLE, Integer.valueOf( style ) );
      reflectionUtil.invoke( result, CREATE_WIDGET, $( valueOf( 0 ), int.class ) );
    }
    return result;
  }

  private static void applyDrawingOrderOrdinalNumber( Control control, int ordinalNumber ) {
    control.moveAbove( control.getParent().getChildren()[ ordinalNumber ] );
  }

  private static Collection<Class<?>> supportedTypes() {
    List<Class<?>> result = new ArrayList<Class<?>>();
    result.add( TreeAdapter.class );
    result.add( TableAdapter.class );
    return unmodifiableList( result );
  }
}