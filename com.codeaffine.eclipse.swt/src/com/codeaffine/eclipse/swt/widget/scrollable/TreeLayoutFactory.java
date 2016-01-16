package com.codeaffine.eclipse.swt.widget.scrollable;

import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Tree;

import com.codeaffine.eclipse.swt.widget.scrollable.context.AdaptionContext;
import com.codeaffine.eclipse.swt.widget.scrollbar.FlatScrollBar;

class TreeLayoutFactory extends ScrollableLayoutFactory<Tree> {

  @Override
  public Layout create( AdaptionContext<Tree> context, FlatScrollBar horizontal, FlatScrollBar vertical ) {
    return new ScrollableLayout( newContext( context ), horizontal, vertical, getCornerOverlay() );
  }

  @Override
  public SelectionListener createHorizontalSelectionListener( AdaptionContext<Tree> context ) {
    return new HorizontalSelectionListener( context );
  }

  @Override
  public SelectionListener createVerticalSelectionListener( AdaptionContext<Tree> context ) {
    return new TreeVerticalSelectionListener( context );
  }

  @Override
  public DisposeListener createWatchDog(
    AdaptionContext<Tree> context, FlatScrollBar horizontal, FlatScrollBar vertical )
  {
    Tree control = context.getScrollable().getControl();
    TreeVerticalScrollBarUpdater updater = new TreeVerticalScrollBarUpdater( control, vertical );
    return new WatchDog( newContext( context ), updater );
  }

  private static AdaptionContext<Tree> newContext( AdaptionContext<Tree> context ) {
    return context.newContext( context.getScrollable().getItemHeight() );
  }
}