package com.github.sbridges.objectinspector;

/*
 * Copyright 2000 Sean Bridges. All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are
 * permitted provided that the following conditions are met:
 * 
 *    1. Redistributions of source code must retain the above copyright notice, this list of
 *       conditions and the following disclaimer.
 * 
 *    2. Redistributions in binary form must reproduce the above copyright notice, this list
 *       of conditions and the following disclaimer in the documentation and/or other materials
 *       provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY <COPYRIGHT HOLDER> ''AS IS'' AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 * The views and conclusions contained in the software and documentation are those of the
 * authors and should not be interpreted as representing official policies, either expressed
 * 
 * 
 */

import java.awt.BorderLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;


/**
 *
 * <p>
 * The inspector is a simple Object inspector.  It allows you to inspect
 * objects at runtime, seeing their public, protected and private variables.
 *  </p> <p>
 *
 * Generally you use Inspector.inspect( some object) or
 * Inspector.inspectAndWait( some object).  The second method does not
 * return until the inspector window is closed. You can also create an
 * Inspector panel directly by creating a new Inspector instance.
 * The inspector extends JPanel. </p> <p>
 *
 * The program uses the reflection and swing API's and requires jdk1.2 or greater.
 * </p> <p>
 *
 * The values that the inspector shows are created dynamically.  That is if you
 * open an inspector window on an object, and change the values of the objects
 * fields, then when you display the fields in the inspector the new values
 * will be used.  </p> <p>
 *
 * At times the structor of an object will change.  For instance if you have
 * a Component field called comp, with a reference to a Button object when
 * the inspector window is created, then change comp to be a JButton, the
 * hierarchy of the inspector tree will change.  If this happens the inspector
 * tree will collapse and refresh itself. </p>
 *
 * @author Sean Bridges
 * @see <a href="https://github.com/sbridges/object-inspector">more info
 * @version 0.1
 */


public class Inspector extends JPanel
{

//-----------------------------------
  //instance variables
  private Object inspecting; //the object that we are inspecting

  private JTree tree;
  private JTextArea text;
  private InspectorNode root;


//-----------------------------------
  //class methods

  /**
   * A test, inspects a convoluted class instance.
   * @see ConvolutedClass
   */
  public static void main(String[] args)
  {
    /*try{
    PrintStream err= new PrintStream( new FileOutputStream("javaerr" + System.currentTimeMillis()));
    System.setErr(err);} catch(Exception e){}*/


    inspectAndWait(new ConvolutedClass() ) ;
    System.exit(0);
  }

  /**
   * Open an inspector window on an instance of an object.
   */
  public final static JFrame inspect(Object obj)
  {
    JFrame f = 	getInspectorWindow(obj);
    f.addWindowListener(new InspectorWindowListener(null));
    f.show();
    return f;
  }

  /**
   * Open an inspector window on an instance of an object.
   * Call does not return until the inspector window is closed.
   **/
   public final static void inspectAndWait(Object obj)
   {
    JFrame f = 	getInspectorWindow(obj);

    Object lock = new Object();

    //sychonize the opening on the lock so the lock
    //cant be notified until we wait.
    synchronized(lock)
    {
      f.addWindowListener(new InspectorWindowListener(lock));
      f.show();
      while(f.isVisible())
      {
        try
        {
          lock.wait();
        }
        catch(InterruptedException e)
        {}
      }
    }

   }



   private final static JFrame getInspectorWindow(Object obj)
   {
    String name;
    if(obj == null)
    {
      name = "null";
    }
    else
    {
      name = "a " + obj.getClass().getName() + " - " + obj;
    }

    JPanel p = new Inspector(obj);
    JFrame f = new JFrame(name);
    f.getContentPane().add(p, BorderLayout.CENTER);
    f.setSize(550,400);
    f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    return f;
   }

//-----------------------------------
  //constructors

  /**
   * Create a new Inspector panel inspecting null
   */
  public Inspector()
  {
    super();
    init(null);
  }

  /**
   * Create a new Inspector panel inspecting the given object
   */
  public Inspector(Object obj)
  {
    super();
    init(obj);
  }

  /**
   * Create a new Inspector panel inspecting the given object.
   * Anagalous to the JPanel(boolean) constructor.
   */
  public Inspector(boolean doubleBuffered, Object obj)
  {
    super(doubleBuffered);
    init(obj);
  }

//---------------------------------
  //instance methods

//---------------------------------
  //initialization

  private void init(Object obj)
  {
    tree = new JTree();
    setInspecting(obj);

    DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer();
    renderer.setLeafIcon(null);
    renderer.setClosedIcon(null);
    renderer.setOpenIcon(null);
    tree.setCellRenderer(renderer);

    tree.addTreeSelectionListener(new SelectionListener() );

    text = new JTextArea();
    text.setLineWrap(true);
    text.setWrapStyleWord(true);

    JScrollPane left = new JScrollPane(tree);
    JScrollPane right = new JScrollPane(text);
    JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, left,right);

    this.setLayout(new BorderLayout() );
    this.add(splitPane, BorderLayout.CENTER);
    splitPane.setDividerLocation(350);

  }

//---------------------------------
  //getters and setters

  public Object getInspecting()
  {
    return inspecting;
  }

  public void setInspecting(Object obj)
  {
    inspecting = obj;
    root = ComplexNode.createInspectorTree(inspecting);
    tree.setModel(new DefaultTreeModel(root) );
  }


  class SelectionListener implements TreeSelectionListener
  {
    public void valueChanged(TreeSelectionEvent e)
    {
      Object last = tree.getLastSelectedPathComponent();
      if(last != null)
      {
        InspectorNode in = (InspectorNode) last;
        if(!in.isValid())
        {
          //if a node is invalid, reset
          System.err.println("reseting root value");
          System.err.println(root.getValue() );
          setInspecting(root.getValue() );

        }
        else
        {
          text.setText(in.getValueString() );
        }
      }
    }
  }


}

class InspectorWindowListener extends WindowAdapter
{

  private Object lock;

  InspectorWindowListener(Object lock)
  {
    this.lock = lock;
  }


  public void windowClosed(WindowEvent e)
  {
    if(lock != null)
    {
      synchronized(lock)
      {
        lock.notifyAll();
      }
    }
    e.getWindow().removeNotify();
  }
}

