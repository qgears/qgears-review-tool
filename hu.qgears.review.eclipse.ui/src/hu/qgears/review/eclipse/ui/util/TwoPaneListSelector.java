package hu.qgears.review.eclipse.ui.util;

import hu.qgears.commons.UtilEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

/**
 * List selector with two panels. The user is able to select elements by moving
 * them from the right hand side list to left hand side list.
 * 
 * @author agostoni
 * 
 * @param <ElementType> The type of elements in this selector.
 * @since 3.0
 */
public class TwoPaneListSelector<ElementType> extends Composite {

	/**
	 * If the user selects an item in a panel, then the selection in other panel
	 * must be cleared. This listener forces this behavior in list viewers.
	 * 
	 * @author agostoni
	 * 
	 */
	private class SelectionSynchronizer implements ISelectionChangedListener {
		@Override
		public void selectionChanged(SelectionChangedEvent event) {
			if (!event.getSelection().isEmpty()){
				if (event.getSelectionProvider() == rightPanel){
					leftPanel.setSelection(StructuredSelection.EMPTY);
				} else {
					rightPanel.setSelection(StructuredSelection.EMPTY);
				}
				updateEnabledState();
			}
		}
	}
	/**
	 * Moves the selected elements in src viewer into target viewer.
	 * 
	 * @author agostoni
	 *
	 */
	private class MoveAction extends SelectionAdapter {

		private final ListViewer src;
		private final ListViewer trg;

		public MoveAction(ListViewer src, ListViewer trg) {
			this.src = src;
			this.trg = trg;
		}

		@Override
		public void widgetSelected(SelectionEvent e) {
			ElementType currentSelection = getSelction(src);
			if (currentSelection != null){
				ArrayList<ElementType> srcInput = new ArrayList<ElementType>(getInputFrom(src));
				srcInput.remove(currentSelection);
				
				ArrayList<ElementType> trgInput = new ArrayList<ElementType>(getInputFrom(trg));
				trgInput.add(currentSelection);
				src.setInput(srcInput);
				trg.setInput(trgInput);
				src.setSelection(srcInput.isEmpty() ? StructuredSelection.EMPTY : new StructuredSelection(srcInput.get(0)));
			}
			updateEnabledState();
			selectionChangedEvent.eventHappened(null);
		}

	}
	/**
	 * Moves all elements from source viewer to target viewer.
	 * 
	 * @author agostoni
	 *
	 */
	private class MoveAllAction extends SelectionAdapter {
		
		private final ListViewer src;
		private final ListViewer trg;
		
		public MoveAllAction(ListViewer src, ListViewer trg) {
			this.src = src;
			this.trg = trg;
		}
		
		@Override
		public void widgetSelected(SelectionEvent e) {
			List<ElementType> input = getInputFrom(src);
			src.setInput(Collections.emptyList());
			ArrayList<ElementType> newInput = new ArrayList<ElementType>(getInputFrom(trg));
			newInput.addAll(input);
			trg.setInput(newInput);
			updateEnabledState();
			selectionChangedEvent.eventHappened(null);
		}
	}

	private ListViewer leftPanel;
	private ListViewer rightPanel;
	private Button moveLeft;
	private Button moveRight;
	
	private Class<ElementType> elementType;
	private Button moveAllRight;
	private Button moveAllLeft;
	private UtilEvent<Object> selectionChangedEvent = new UtilEvent<Object>();
	public TwoPaneListSelector(Composite parent, int style,Class<ElementType> elementType) {
		super(parent, style);
		this.elementType = elementType;
		init();
	}

	private void init() {
		setLayout(new GridLayout(3,false));
		leftPanel = new ListViewer(this,SWT.BORDER | SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL);
		leftPanel.getControl().setLayoutData(new GridData(GridData.FILL_BOTH));
		leftPanel.setContentProvider(ArrayContentProvider.getInstance());
		
		Composite buttonGroup = new Composite(this, SWT.NONE);
		buttonGroup.setLayoutData(new GridData(SWT.FILL,SWT.CENTER,false,true));
		
		rightPanel = new ListViewer(this,SWT.BORDER| SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL);
		rightPanel.getControl().setLayoutData(new GridData(GridData.FILL_BOTH));
		rightPanel.setContentProvider(ArrayContentProvider.getInstance());

		SelectionSynchronizer ss = new SelectionSynchronizer();
		rightPanel.addSelectionChangedListener(ss);
		leftPanel.addSelectionChangedListener(ss);
		
		initButtons(buttonGroup);
	}

	private void initButtons(Composite buttonGroup) {
		buttonGroup.setLayout(new GridLayout(1,true));
		moveRight = new Button(buttonGroup, SWT.NONE);
		moveRight.setText(">");
		moveRight.addSelectionListener(new MoveAction(leftPanel,rightPanel));
		moveRight.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		moveAllRight = new Button(buttonGroup, SWT.NONE);
		moveAllRight.setText(">>");
		moveAllRight.addSelectionListener(new MoveAllAction(leftPanel,rightPanel));
		moveAllRight.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		moveLeft = new Button(buttonGroup, SWT.NONE);
		moveLeft.setText("<");
		moveLeft.addSelectionListener(new MoveAction(rightPanel,leftPanel));
		moveLeft.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		moveAllLeft = new Button(buttonGroup, SWT.NONE);
		moveAllLeft.setText("<<");
		moveAllLeft.addSelectionListener(new MoveAllAction(rightPanel,leftPanel));
		moveAllLeft.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		updateEnabledState();
	}

	private void updateEnabledState() {
		moveLeft.setEnabled(!rightPanel.getSelection().isEmpty());
		moveRight.setEnabled(!leftPanel.getSelection().isEmpty());

		moveAllRight.setEnabled(!getInputFrom(leftPanel).isEmpty());
		moveAllLeft.setEnabled(!getInputFrom(rightPanel).isEmpty());
		leftPanel.refresh();
		rightPanel.refresh();
	}

	/**
	 * Set the label provider that must be used to render labels of items in lists.
	 * 
	 * @param lp
	 */
	public void setLabelProvider(ILabelProvider lp){
		leftPanel.setLabelProvider(lp);
		rightPanel.setLabelProvider(lp);
	}
	
	/**
	 * Set the selector input
	 * 
	 * @param choices The possible options that the user can select.
	 * @param initialSelection The initial selection
	 */
	public void setInput(List<ElementType> choices,List<ElementType> initialSelection){
		if (choices.containsAll(initialSelection)){
			List<ElementType> leftcontent = new ArrayList<ElementType>(choices);
			leftcontent.removeAll(initialSelection);
			leftPanel.setInput(leftcontent);
			rightPanel.setInput(new ArrayList<ElementType>(initialSelection));
			updateEnabledState();
			selectionChangedEvent.eventHappened(null);
		} else {
			throw new RuntimeException("Initial selection must be part of possible choices");
		}
	}
	
	/**
	 * Returns the selected items in same order as appear in viewer.
	 * 
	 * @return
	 */
	public List<ElementType> getSelectedElements(){
		return getInputFrom(rightPanel);
	}

	private List<ElementType> getInputFrom(ListViewer panel) {
		@SuppressWarnings("unchecked")
		List<ElementType> list = (List<ElementType>) Arrays.asList(((ArrayContentProvider)panel.getContentProvider()).getElements(panel.getInput()));
		return Collections.checkedList(list, elementType);
	}
	

	@SuppressWarnings("unchecked")
	private ElementType getSelction(ListViewer panel) {
		ISelection s = panel.getSelection();
		if (s != null && !s.isEmpty()){
			if (s instanceof StructuredSelection){
				StructuredSelection ss = (StructuredSelection) s;
				return (ElementType) ss.getFirstElement();
			}
		}
		return null;
	}
	
	/**
	 * This event is fired when the list of selected items is changing. The
	 * event parameter is always <code>null</code>, use
	 * {@link #getSelectedElements()} in event handler to retrieve currently
	 * selected elements.
	 * 
	 * @return
	 */
	public UtilEvent<Object> getSelectionChangedEvent() {
		return selectionChangedEvent;
	}
}
