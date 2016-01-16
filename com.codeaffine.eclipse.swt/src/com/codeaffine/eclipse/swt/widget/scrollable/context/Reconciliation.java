package com.codeaffine.eclipse.swt.widget.scrollable.context;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Scrollable;

import com.codeaffine.eclipse.swt.widget.scrollable.ScrollbarStyle;

public class Reconciliation {

  final VisibilityReconciliation visibilityReconciliation;
  final EnablementReconciliation enablementReconciliation;
  final BoundsReconciliation boundsReconciliation;
  final LayoutReconciliation layoutReconciliation;
  final ColorReconciliation colorReconciliation;

  Reconciliation( Composite adapter, ScrollableControl<? extends Scrollable> scrollable ) {
    this( new VisibilityReconciliation( adapter, scrollable ),
          new EnablementReconciliation( adapter, scrollable ),
          new BoundsReconciliation( adapter, scrollable ),
          new LayoutReconciliation( adapter, scrollable ),
          new ColorReconciliation( castToScrollbarStyleIfPossible( adapter ), scrollable ) );
  }

  Reconciliation( VisibilityReconciliation visibilityReconciliation,
                  EnablementReconciliation enablementReconciliation,
                  BoundsReconciliation boundsReconciliation,
                  LayoutReconciliation layoutReconciliation,
                  ColorReconciliation colorReconciliation  )
  {
    this.visibilityReconciliation = visibilityReconciliation;
    this.enablementReconciliation = enablementReconciliation;
    this.boundsReconciliation = boundsReconciliation;
    this.layoutReconciliation = layoutReconciliation;
    this.colorReconciliation = colorReconciliation;
  }

  public void runWithSuspendedBoundsReconciliation( Runnable runnable ) {
    boundsReconciliation.runSuspended( runnable );
  }

  public boolean setVisible( boolean visible ) {
    return visibilityReconciliation.setVisible( visible );
  }

  public boolean setEnabled( boolean enabled ) {
    return enablementReconciliation.setEnabled( enabled );
  }

  public void runWhileSuspended( Runnable runnable ) {
    suspend();
    try {
      runnable.run();
    } finally {
      resume();
    }
  }

  static ScrollbarStyle castToScrollbarStyleIfPossible( Composite adapter ) {
    return adapter instanceof ScrollbarStyle ? ( ScrollbarStyle )adapter : null;
  }

  private void suspend() {
    boundsReconciliation.suspend();
  }

  private void resume() {
    visibilityReconciliation.run();
    enablementReconciliation.run();
    boundsReconciliation.resume();
    boundsReconciliation.run();
    layoutReconciliation.run();
    colorReconciliation.run();
  }
}