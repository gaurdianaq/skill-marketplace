import 'styled-components';

declare module 'styled-components' {
  export interface DefaultTheme {
    headerHeight?: string;
    backgroundColor?: string;
    strokeColor?: string;
    bs?: string;
    textFont?: string;
    accent?: string;
    color: {
      primary: string;
      black?: string;
      gray?: string;
    };
    changeOpacity: (color: string, opacity: number) => string;
    tailwind: {
      [color: string]: string;
    };
  }
}
