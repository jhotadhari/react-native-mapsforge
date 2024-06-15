const eslintRecommended = require( '@eslint/js' ).configs.recommended;
const reactRecommended = require( 'eslint-plugin-react/configs/recommended' );
const reactNativeRecommended = require( '@react-native/eslint-config' );
const react = require( 'eslint-plugin-react' );
const reactNative = require( 'eslint-plugin-react-native' );
const reactHooks = require( 'eslint-plugin-react-hooks' );
const comments = require( 'eslint-plugin-eslint-comments' );

const rules = {
	'react/prop-types': 'off',
	'switch-colon-spacing': [
		'error', {
			'after': true,
			'before': false,
		},
	],
	'no-mixed-spaces-and-tabs': 'error',
	'indent': [
		'error',
		'tab',
		{
			'FunctionDeclaration': {
				'body': 1,
				'parameters': 1,
			},
			'FunctionExpression': {
				'body': 1,
				'parameters': 1,
			},
			'SwitchCase': 1,
			'VariableDeclarator': 1,
			'outerIIFEBody': 1,
			'CallExpression': { 'arguments': 1 },
			'MemberExpression': 1,
			'ArrayExpression': 1,
			'ObjectExpression': 1,
			'ImportDeclaration': 1,
		},
	],
	'linebreak-style': [
		'error',
		'unix',
	],
	'quotes': [
		'error',
		'single',
	],
	'rest-spread-spacing': [
		'error',
		'never',
	],
	'space-in-parens': [
		'error',
		'always',
	],
	'space-before-blocks': [
		'error',
		'always',
	],
	'space-before-function-paren': [
		'error',
		'never',
	],
	// 'keyword-spacing': 'error',
	'space-unary-ops': [
		'error',
		{
			'words': true,
			'nonwords': true,
			'overrides': { '-': false },
		},
	],
	'semi': [
		'error',
		'always',
	],
	'comma-style': [
		'error',
		'last',
	],
	'brace-style': [
		'error',
		'1tbs',
	],
	'comma-dangle': [
		'error',
		'always-multiline',
	],
	'prefer-arrow-callback': 'error',
	'no-trailing-spaces': 'error',
	'object-curly-spacing': [
		'error',
		'always',
	],
	'object-curly-newline': [
		'error', {
			'ImportDeclaration': {
				'multiline': true,
				'minProperties': 3,
			},
			'ObjectExpression': {
				'multiline': true,
				'minProperties': 3,
			},
			'ObjectPattern': {
				'multiline': true,
				'minProperties': 3,
			},
			'ExportDeclaration': {
				'multiline': true,
				'minProperties': 3,
			},

		},
	],
	'newline-per-chained-call': ['error', { 'ignoreChainWithDepth': 2 }],
	'object-property-newline': [
		'error',
		{ 'allowAllPropertiesOnSameLine': true },
	],
	'comma-spacing': [
		'error', {
			'before': false,
			'after': true,
		},
	],
	'array-bracket-newline': [
		'error', {
			'multiline': true,
			'minItems': 3,
		},
	],
	'array-element-newline': [
		'error',
		'consistent',
	],
	'no-unused-vars': ['error', { 'varsIgnorePattern': 'React' }],
};

module.exports = [
	{
		files: [
			'**/babel.config.js',
			'**/eslint.config.js',
			'**/jest.config.js',
			'**/metro.config.js',
			'**/react-native.config.js',
		],
		languageOptions: {
			globals: {
				'__dirname': true,
				'console': true,
			},
			'sourceType': 'commonjs',
			'ecmaVersion': 'latest',
		},
		'rules': {
			...eslintRecommended.rules,
			...rules,
		},
	},
	{
		...reactRecommended,
		files: [
			'src/**/*.js',
			'src/**/*.jsx',
			'example/src/**/*.js',
			'example/src/**/*.jsx',
		],
		languageOptions: {
			...reactRecommended.languageOptions,
			globals: {
				'react-native/react-native': true,
				'console': true,
			},
			'ecmaVersion': 'latest',
			'sourceType': 'module',
			'parserOptions': { 'ecmaFeatures': { 'jsx': true } },
		},
		'plugins': {
			comments,
			react,
			reactNative,
			reactHooks,
		},
		'settings': { 'react': { 'version': 'detect' } },
		'rules': {
			...eslintRecommended.rules,
			...reactRecommended.rules,
			...( Object.keys( reactNativeRecommended.rules ).reduce( ( acc, key ) => {
				if (
					key.startsWith( 'eslint-comments/' )
					|| key.startsWith( 'react-hooks/' )
					|| key.startsWith( 'react-native/' )
					|| key.startsWith( 'jest/' )
				) {
					return acc;
				} else {
					return {
						...acc,
						[key]: reactNativeRecommended.rules[key],
					};
				}
			}, {} ) ),
			...rules,
			'react/jsx-closing-bracket-location': [
				'error',
				'line-aligned',
			],
			'react/destructuring-assignment': [
				'error',
				'always',
			],
			'react/jsx-boolean-value': [
				'error',
				'always',
			],
			'react/jsx-curly-spacing': [
				'error',
				'always',
			],
			'react/jsx-equals-spacing': [
				'error',
				'never',
			],
			'react/jsx-indent': [
				'error',
				'tab',
			],
			'react/jsx-indent-props': [
				'error',
				'tab',
			],
			'react/jsx-props-no-multi-spaces': 'error',
			'react/jsx-first-prop-new-line': [
				'error',
				'multiline',
			],
			'react/function-component-definition': [
				'error',
				{ 'namedComponents': 'arrow-function' },
			],
			'react/jsx-tag-spacing': [
				'error',
				{
					'closingSlash': 'never',
					'beforeSelfClosing': 'always',
					'afterOpening': 'never',
					'beforeClosing': 'never',
				},
			],
		},
	},
];
