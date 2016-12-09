


declare namespace JboxNamespace {
    interface JboxOptions
    {
        attach: string,
        title: string,
        content: string
    }
    class Jbox {
        constructor(type: string, options: JboxOptions);

    }
    enum JboxType {
        Tooltip,
        Modal
    }
}



declare module "jbox" {
   export = JboxNamespace

}

declare namespace CypTooltip {
    var Tooltip:any;
}

declare module "@cypress/react-tooltip" {
import ComponentClass = __React.ComponentClass;


    class Tooltip extends __React.Component<any,any>{
    }
    export default Tooltip;
}

