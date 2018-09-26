/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lt.lb.commons.interfaces;

/**
 *
 * @author Laimonas-Beniusis-PC
 */
public interface StringBuilderActions {

    public static interface ILineAppender {

        public static final ILineAppender empty = new ILineAppender() {
            @Override
            public ILineAppender appendLine(Object... objs) {
                return empty;
            }
        };

        public ILineAppender appendLine(Object... objs);
    }

    public static interface ILineInserter {

        public ILineInserter insertLine(int pos, Object... objs);

    }

    public static interface ILinePrepender {

        public ILinePrepender prependLine(Object... objs);
    }

    public static interface IAppender {

        public IAppender append(Object... objs);
    }

    public static interface IPrepender {

        public IPrepender prepend(Object... objs);
    }

    public static interface IInserter {

        public IInserter insert(int pos, Object... objs);
    }

    public static interface IStandardStringBuilder extends IAppender, IInserter {

    }

    public static interface ILineStringBuilder extends ILineAppender, ILineInserter, ILinePrepender, IAppender, IPrepender, IInserter {
    }
}
