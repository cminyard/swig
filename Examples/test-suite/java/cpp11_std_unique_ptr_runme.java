import cpp11_std_unique_ptr.*;

public class cpp11_std_unique_ptr_runme {
  static {
    try {
        System.loadLibrary("cpp11_std_unique_ptr");
    } catch (UnsatisfiedLinkError e) {
      System.err.println("Native code library failed to load. See the chapter on Dynamic Linking Problems in the SWIG Java documentation for help.\n" + e);
      System.exit(1);
    }
  }

  private static void WaitForGC()
  {
    System.gc();
    System.runFinalization();
    try {
      java.lang.Thread.sleep(10);
    } catch (java.lang.InterruptedException e) {
    }
  }

  private static void checkCount(int expected_count) {
    int actual_count = Klass.getTotal_count();
    if (actual_count != expected_count)
      throw new RuntimeException("Counts incorrect, expected:" + expected_count + " actual:" + actual_count);
  }

  public static void main(String argv[]) throws Throwable
  {
    // unique_ptr as input
    {
      Klass kin = new Klass("KlassInput");
      checkCount(1);
      String s = cpp11_std_unique_ptr.takeKlassUniquePtr(kin);
      checkCount(0);
      if (!s.equals("KlassInput"))
        throw new RuntimeException("Incorrect string: " + s);
      if (!cpp11_std_unique_ptr.is_nullptr(kin))
        throw new RuntimeException("is_nullptr failed");
      kin.delete(); // Should not fail, even though already deleted
      checkCount(0);
    }

    {
      Klass kin = new Klass("KlassInput");
      checkCount(1);
      String s = cpp11_std_unique_ptr.takeKlassUniquePtr(kin);
      checkCount(0);
      if (!s.equals("KlassInput"))
        throw new RuntimeException("Incorrect string: " + s);
      if (!cpp11_std_unique_ptr.is_nullptr(kin))
        throw new RuntimeException("is_nullptr failed");
      boolean exception_thrown = false;
      try {
        cpp11_std_unique_ptr.takeKlassUniquePtr(kin);
      } catch (RuntimeException e) {
        if (!e.getMessage().contains("Cannot release ownership as memory is not owned"))
          throw new RuntimeException("incorrect exception message");
        exception_thrown = true;
      }
      if (!exception_thrown)
          throw new RuntimeException("double usage of takeKlassUniquePtr should have been an error");
      kin.delete(); // Should not fail, even though already deleted
      checkCount(0);
    }

    {
      Klass kin = new Klass("KlassInput");
      boolean exception_thrown = false;
      try {
        Klass notowned = cpp11_std_unique_ptr.get_not_owned_ptr(kin);
        cpp11_std_unique_ptr.takeKlassUniquePtr(notowned);
      } catch (RuntimeException e) {
        if (!e.getMessage().contains("Cannot release ownership as memory is not owned"))
          throw new RuntimeException("incorrect exception message");
        exception_thrown = true;
      }
      if (!exception_thrown)
        throw new RuntimeException("Should have thrown 'Cannot release ownership as memory is not owned' error");
      kin.delete();
      checkCount(0);
    }

    {
      KlassInheritance kini = new KlassInheritance("KlassInheritanceInput");
      checkCount(1);
      String s = cpp11_std_unique_ptr.takeKlassUniquePtr(kini);
      checkCount(0);
      if (!s.equals("KlassInheritanceInput"))
        throw new RuntimeException("Incorrect string: " + s);
      if (!cpp11_std_unique_ptr.is_nullptr(kini))
        throw new RuntimeException("is_nullptr failed");
      kini.delete(); // Should not fail, even though already deleted
      checkCount(0);
    }

    // unique_ptr as output
    Klass k1 = cpp11_std_unique_ptr.makeKlassUniquePtr("first");
    if (!k1.getLabel().equals("first"))
      throw new RuntimeException("wrong object label");

    Klass k2 = cpp11_std_unique_ptr.makeKlassUniquePtr("second");
    if (Klass.getTotal_count() != 2)
      throw new RuntimeException("number of objects should be 2");

    k1 = null;
    {
      int countdown = 500;
      int expectedCount = 1;
      while (true) {
        WaitForGC();
        if (--countdown == 0)
          break;
        if (Klass.getTotal_count() == expectedCount)
          break;
      }
      int actualCount = Klass.getTotal_count();
      if (actualCount != expectedCount)
        System.err.println("GC failed to run (cpp11_std_unique_ptr 1). Expected count: " + expectedCount + " Actual count: " + actualCount); // Finalizers are not guaranteed to be run and sometimes they just don't
    }

    if (!k2.getLabel().equals("second"))
      throw new RuntimeException("wrong object label");

    k2 = null;
    {
      int countdown = 500;
      int expectedCount = 0;
      while (true) {
        WaitForGC();
        if (--countdown == 0)
          break;
        if (Klass.getTotal_count() == expectedCount)
          break;
      };
      int actualCount = Klass.getTotal_count();
      if (actualCount != expectedCount)
        System.err.println("GC failed to run (cpp11_std_unique_ptr 2). Expected count: " + expectedCount + " Actual count: " + actualCount); // Finalizers are not guaranteed to be run and sometimes they just don't
    }
  }
}